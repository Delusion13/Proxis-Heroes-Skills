package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillWrestle extends ActiveSkill {
    
    public SkillWrestle(Heroes plugin) {
        super(plugin, "Wrestle");
        setDescription("$1s stun + $3 damage for all players within $2 blocks you.");
        setUsage("/skill wrestle");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill wrestle" });
        
        setTypes(SkillType.PHYSICAL, SkillType.DEBUFF, SkillType.MOVEMENT, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 3, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", radius + "").replace("$3", damage + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }
    
    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.DURATION.node(), 10000);
        node.set("duration-increase", 0);
        node.set(Setting.RADIUS.node(), 3);
        node.set("radius-increase",0);
        node.set(Setting.DAMAGE.node(), 0);
        node.set("damage-increase", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 3, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        radius = radius < 2 ? 3 : radius;
        List<Entity> entities = hero.getPlayer().getNearbyEntities(radius, radius, radius);
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        StunEffect cEffect = new StunEffect(this, duration);
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        
        for (Entity n : entities) {
            if (n instanceof Player && damageCheck(player, (Player) n)) {
                Player p = (Player) n;
                Hero tHero = plugin.getCharacterManager().getHero(p);
                tHero.addEffect(cEffect);
                if (damage > 0) {
                    damageEntity(p, player, damage, DamageCause.MAGIC);
                    //p.damage(damage, player);
                }
            }
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

}

