# Visual & Sound Effects

OumLib provides dynamic, chainable builders for playing particles and sound effects under the `dev.oum.oumlib.effect` package.

---

## Real-world Example: Level Up Helix

Play a chime sound and render a golden spiral helix around a player whenever they level up:

```java
import dev.oum.oumlib.effect.Effects;
import dev.oum.oumlib.effect.Particles;
import dev.oum.oumlib.effect.SoundBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class LevelUpAnimation {
    public void animate(Player player) {
        Location center = player.getLocation();
        
        Effects.sound(Sound.ENTITY_PLAYER_LEVELUP)
            .volume(1.0F)
            .pitch(1.2F)
            .play(player);

        Particles.spawnHelix(
            center,
            1.0,
            0.2,
            2.0,
            50,
            Effects.particle(Particle.DUST).color(Color.YELLOW, 1.0F)
        );
    }
}
```

---

## Real-world Example: Gun Bullet Tracer

Draws a line of smoke particles representing a gun tracer from the player's eye location to their target hit point, playing a gunshot sound:

```java
import dev.oum.oumlib.effect.Effects;
import dev.oum.oumlib.effect.Particles;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class WeaponTracer {
    public void fireTracer(Player player, Location targetLoc) {
        Location origin = player.getEyeLocation();
        
        Effects.sound(Sound.ENTITY_FIREWORK_ROCKET_BLAST)
            .volume(0.8F)
            .pitch(1.5F)
            .play(origin);

        Particles.spawnLine(
            origin,
            targetLoc,
            Effects.particle(Particle.CRIT).count(1),
            15
        );
    }
}
```

---

## Static Sound & Particle Spawners

Trigger audio files or play standard dust options using fast static shortcuts:

```java
import dev.oum.oumlib.effect.Particles;
import dev.oum.oumlib.effect.Sounds;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class VisualShortcuts {
    public void execute(Player player, Location location) {
        Particles.spawnDust(location, Color.RED, 1.2F, 10);
        Sounds.play(player, "block.note_block.pling", 1.0F, 1.2F);
    }
}
```
