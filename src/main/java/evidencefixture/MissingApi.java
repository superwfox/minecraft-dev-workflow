package evidencefixture;
import io.papermc.paper.event.player.PlayerShieldBlockEvent;
import org.bukkit.Particle;
public final class MissingApi {
    private PlayerShieldBlockEvent event;
    public Object missingParticle() { return Particle.SLIME; }
}
