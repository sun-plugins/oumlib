# Mathematical Utilities

OumLib features a platform-independent mathematical package `dev.oum.oumlib.math` optimized for 3D physics, spatial partitioning, expressions evaluation, and noise generation.

---

## Real-world Example: Regional Claim Protection Zone

Here is a protection system that maps safe-zone regions (Sphere, Cylinder, or AABB boxes) and checks if a player is standing inside the safe zone:

```java
import dev.oum.oumlib.math.Vector3D;
import dev.oum.oumlib.math.Volume3D;
import dev.oum.oumlib.math.Volume3D.AABB3D;
import dev.oum.oumlib.math.Volume3D.Cylinder3D;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public final class ProtectionZoneManager {
    private final List<Volume3D> safeZones = new ArrayList<>();

    public void createZones() {
        safeZones.add(new AABB3D(new Vector3D(-100, 0, -100), new Vector3D(100, 256, 100)));
        safeZones.add(new Cylinder3D(new Vector3D(200, 64, 200), 15.0, 10.0));
    }

    public boolean isSafe(Player player) {
        Location loc = player.getLocation();
        Vector3D pt = new Vector3D(loc.getX(), loc.getY(), loc.getZ());
        
        for (Volume3D zone : safeZones) {
            if (zone.contains(pt)) {
                return true;
            }
        }
        return false;
    }
}
```

---

## Real-world Example: Dynamic Skill Damage Evaluation

Evaluate dynamic math formulas loaded from config files (e.g. `base_dmg * (1.5 ^ level)`), replacing variables with active player levels dynamically:

```java
import dev.oum.oumlib.math.MathEval;
import dev.oum.oumlib.text.Text;
import org.bukkit.entity.Player;
import java.util.Map;

public final class SkillDamageEvaluator {
    public void castSkill(Player player, String formula) {
        MathEval eval = new MathEval(formula);
        double dmg = eval.evaluate(Map.of(
            "level", (double) player.getLevel(),
            "base_dmg", 10.0
        ));

        Text.send(player, "<gold>You dealt " + dmg + " damage!</gold>");
    }
}
```

---

## Easing Animations

Standard mathematical easing functions for UI displays:

```java
import dev.oum.oumlib.math.Easing;

public final class AnimationPlotter {
    public double getProgress(double time) {
        return Easing.BOUNCE_OUT.apply(time);
    }
}
```

---

## Vector3D Reference

Immutable Vector operations:

```java
import dev.oum.oumlib.math.Vector3D;

public final class VectorMath {
    public void calculate() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 5.0, 6.0);

        Vector3D added = v1.add(v2);
        Vector3D dot = v1.multiply(v2.normalize());
    }
}
```

---

## Loot Table Chance Rolles

Determine loot rewards using weights:

```java
import dev.oum.oumlib.math.Chance;
import dev.oum.oumlib.math.WeightedSelector;
import java.util.Map;

public final class LootRoller {
    public String rollReward() {
        if (Chance.percent(5.0)) {
            return "special_crate";
        }

        WeightedSelector<String> selector = WeightedSelector.of(Map.of(
            "gold", 70.0,
            "diamond", 25.0,
            "netherite", 5.0
        ));
        return selector.select();
    }
}
```
