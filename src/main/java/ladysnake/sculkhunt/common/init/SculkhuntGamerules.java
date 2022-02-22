package ladysnake.sculkhunt.common.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class SculkhuntGamerules {
    public static GameRules.Key<GameRules.BooleanRule> SCULK_CATALYST_SPAWNING;
    public static GameRules.Key<GameRules.IntRule> SCULK_CATALYST_SPAWNING_DELAY;
    public static GameRules.Key<GameRules.IntRule> SCULK_CATALYST_SPAWNING_RADIUS;
    public static GameRules.Key<GameRules.IntRule> SCULK_CATALYST_BLOOM_DELAY;
    public static GameRules.Key<GameRules.IntRule> SCULK_CATALYST_BLOOM_RADIUS;
    public static GameRules.Key<GameRules.IntRule> SCULK_CATALYST_TERRITORY_RADIUS;
    public static GameRules.Key<GameRules.IntRule> SCULK_CATALYST_MOB_SPAWN_FREQUENCY;

    public static void init() {
        SCULK_CATALYST_SPAWNING = registerGamerule("sculkCatalystSpawning", GameRuleFactory.createBooleanRule(false));
        SCULK_CATALYST_SPAWNING_DELAY = registerGamerule("sculkCatalystSpawningDelay", GameRuleFactory.createIntRule(1));
        SCULK_CATALYST_SPAWNING_RADIUS = registerGamerule("sculkCatalystSpawningRadius", GameRuleFactory.createIntRule(50));
        SCULK_CATALYST_BLOOM_DELAY = registerGamerule("sculkCatalystBloomDelay", GameRuleFactory.createIntRule(200));
        SCULK_CATALYST_BLOOM_RADIUS = registerGamerule("sculkCatalystBloomRadius", GameRuleFactory.createIntRule(25));
        SCULK_CATALYST_TERRITORY_RADIUS = registerGamerule("sculkCatalystTerritoryRadius", GameRuleFactory.createIntRule(100));
        SCULK_CATALYST_MOB_SPAWN_FREQUENCY = registerGamerule("sculkCatalystMobSpawnFrequency", GameRuleFactory.createIntRule(250));
    }

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> registerGamerule(String name, GameRules.Type<T> type) {
        return GameRuleRegistry.register(name, GameRules.Category.MOBS, type);
    }

}
