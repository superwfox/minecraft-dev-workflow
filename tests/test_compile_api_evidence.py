import hashlib
import importlib.util
import json
import os
import subprocess
import tempfile
import unittest
from unittest.mock import patch
import zipfile
from pathlib import Path

spec = importlib.util.spec_from_file_location("evidence", Path(__file__).parents[1] / "tools/compile_api_evidence.py")
evidence = importlib.util.module_from_spec(spec)
spec.loader.exec_module(evidence)


class InventoryTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temp = tempfile.TemporaryDirectory()
        cls.root = Path(cls.temp.name)
        cls.sources = cls.root / "sources"
        cls.sources.mkdir()
        definitions = {
            "org/bukkit/Particle.java": "package org.bukkit; public enum Particle implements Flags { FLAME; public static String TEXT = \"\\u0000\\ud83d\\ude00\"; }",
            "org/bukkit/Flags.java": "package org.bukkit; public interface Flags { int SLIME = 1; }",
            "privateproject/Secret.java": "package privateproject; public class Secret {}",
        }
        for path, content in definitions.items():
            file = cls.sources / path
            file.parent.mkdir(parents=True, exist_ok=True)
            file.write_text(content)
        cls.classes = cls.root / "classes"
        subprocess.run(["javac", "-encoding", "UTF-8", "--release", "8", "-d", str(cls.classes)] + [str(cls.sources / path) for path in definitions], check=True)
        cls.repository = cls.root / "repository"
        cls.jar = cls.repository / "org/bukkit/bukkit/1.21-R0.1-SNAPSHOT/bukkit-1.21-R0.1-SNAPSHOT.jar"
        cls.jar.parent.mkdir(parents=True)
        with zipfile.ZipFile(cls.jar, "w") as archive:
            for path in cls.classes.rglob("*.class"):
                archive.write(path, path.relative_to(cls.classes))
        cls.pom = cls.root / "pom.xml"
        cls.pom.write_text("<project/>")
        cls.classpath = cls.root / "classpath"
        cls.classpath.write_text(str(cls.jar))

    @classmethod
    def tearDownClass(cls):
        cls.temp.cleanup()

    def test_real_class_fields_and_enum_inventory(self):
        items = evidence.scan_jar(self.jar, 21)
        particle = next(item for item in items if item["name"] == "org.bukkit.Particle")
        self.assertTrue(particle["isEnum"])
        self.assertEqual(particle["enumConstants"], ["FLAME"])
        self.assertIn("TEXT", particle["fields"])
        self.assertEqual(particle["interfaces"], ["org.bukkit.Flags"])
        self.assertIn("SLIME", next(item for item in items if item["name"] == "org.bukkit.Flags")["fields"])
        self.assertFalse(any("privateproject" in json.dumps(item) for item in items))

    def test_actual_fingerprint_and_bound_pom(self):
        report = evidence.collect(self.classpath, self.pom, 21, self.repository, "", verify=lambda *_: None)
        self.assertTrue(report["complete"])
        self.assertEqual(report["pomHash"], hashlib.sha256(self.pom.read_bytes()).hexdigest())
        self.assertEqual(report["dependencies"][0]["fingerprint"], "sha256:" + hashlib.sha256(self.jar.read_bytes()).hexdigest())
        self.assertFalse(report["dependencies"][0]["publicVerified"])

    def test_malformed_class_is_not_an_empty_inventory(self):
        bad = self.root / "broken.jar"
        with zipfile.ZipFile(bad, "w") as archive:
            archive.writestr("org/bukkit/Particle.class", b"truncated")
        with self.assertRaises(evidence.EvidenceError):
            evidence.scan_jar(bad, 21)

    def test_directory_classpath_cannot_prove_absence(self):
        cp = self.root / "directory-cp"
        cp.write_text(str(self.classes))
        with self.assertRaises(evidence.EvidenceError):
            evidence.collect(cp, self.pom, 21, self.repository, "")

    def test_duplicate_entries_rejected(self):
        duplicate = self.root / "duplicate.jar"
        with zipfile.ZipFile(duplicate, "w") as archive:
            archive.writestr("org/bukkit/Particle.class", b"one")
            archive.writestr("org/bukkit/Particle.class", b"two")
        with self.assertRaises(evidence.EvidenceError):
            evidence.scan_jar(duplicate, 21)

    def test_multi_release_uses_selected_java(self):
        jar = self.root / "multi.jar"
        with zipfile.ZipFile(jar, "w") as archive:
            archive.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\r\nMulti-Release: true\r\n\r\n")
            archive.write(self.classes / "org/bukkit/Particle.class", "org/bukkit/Particle.class")
            archive.writestr("META-INF/versions/21/org/bukkit/Particle.class", b"broken-new-version")
        self.assertEqual(len(evidence.scan_jar(jar, 8)), 1)
        with self.assertRaises(evidence.EvidenceError):
            evidence.scan_jar(jar, 21)

    def test_compressed_manifest_is_bounded_before_reading(self):
        jar = self.root / "oversized-manifest.jar"
        with zipfile.ZipFile(jar, "w", compression=zipfile.ZIP_DEFLATED) as archive:
            archive.writestr("META-INF/MANIFEST.MF", "x" * 65537)
        with self.assertRaises(evidence.EvidenceError):
            evidence.scan_jar(jar, 21)

    def test_snapshot_requires_timestamped_matching_artifact(self):
        metadata = self.jar.parent / "maven-metadata-test.xml"
        metadata.write_text("<metadata><versioning><snapshotVersions><snapshotVersion><extension>jar</extension><value>1.21-R0.1-20260101.123456-9</value></snapshotVersion></snapshotVersions></versioning></metadata>")
        identity = evidence.jar_identity(self.jar, self.repository)
        self.assertEqual(evidence.resolved_versions(self.jar, identity), ["1.21-R0.1-20260101.123456-9"])
        self.assertIsNone(evidence.jar_identity(self.jar, self.root / "other-repository"))

    def test_private_coordinates_do_not_fetch_or_certify(self):
        identity = {"groupId": "privateproject", "artifactId": "secret", "version": "1", "classifier": ""}
        self.assertIsNone(evidence.verify_public_artifact(self.jar, identity, "a" * 64, float("inf")))

    def test_public_origin_requires_the_actual_bytes_hash(self):
        identity = evidence.jar_identity(self.jar, self.repository)
        metadata = self.jar.parent / "maven-metadata-test.xml"
        metadata.write_text("<metadata><versioning><snapshotVersions><snapshotVersion><extension>jar</extension><value>1.21-R0.1-20260101.123456-9</value></snapshotVersion></snapshotVersions></versioning></metadata>")
        with patch.object(evidence, "public_download_hash", return_value="b" * 64):
            self.assertIsNone(evidence.verify_public_artifact(self.jar, identity, "a" * 64, float("inf")))
        with patch.object(evidence, "public_download_hash", return_value="a" * 64):
            verified = evidence.verify_public_artifact(self.jar, identity, "a" * 64, float("inf"))
            self.assertTrue(verified["publicVerified"])
            self.assertTrue(verified["sourceUrl"].endswith("1.21-R0.1-20260101.123456-9.jar"))

    def test_incomplete_cli_report_is_nonfatal(self):
        result = subprocess.run(["python3", str(Path(evidence.__file__)), "--java-release", "21", "--classpath", str(self.root / "missing")], capture_output=True, text=True, check=True)
        report = json.loads(result.stdout.split(evidence.MARKER)[1])
        self.assertFalse(report["complete"])
        self.assertNotIn("dependencies", report)
        self.assertNotIn(str(self.root), result.stdout)


if __name__ == "__main__":
    unittest.main()
