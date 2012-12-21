package com.alf;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import com.alf.chararacter.CharacterDamageManager;
import com.alf.chararacter.CharacterManager;
import com.alf.chararacter.classes.AlfClassManager;
import com.alf.chararacter.effect.EffectManager;
import com.alf.chararacter.party.PartyManager;
import com.alf.command.commands.HelpCommand;
import com.alf.listener.AEntityListener;
import com.alf.listener.APlayerListener;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillManager;
import com.alf.util.ConfigManager;
import com.alf.util.DeathManager;
import com.alf.util.Properties;

/**
 * Core plugin for Aelfcraft, handles most everything for base gameplay.
 * @author Eteocles
 */
public class AlfCore extends AlfPlugin {

	//Configuration manager for loading.
	private ConfigManager configManager;
	//Class manager for Alfs.
	private AlfClassManager classManager;
	//Effect manager for all Characters.
	private EffectManager effectManager;
	//Character manager for loading, etc.
	private CharacterManager characterManager;
	//Party manager for handling player groups.
	private PartyManager partyManager;
	//Damage manager for all damage events.
	private CharacterDamageManager damageManager;
	//Skill manager for handling all character skill usage.
	private SkillManager skillManager;
	//Skill configuration.
	private SkillConfigManager skillConfigs;
	//Death manager for handling player deaths.
	private DeathManager deathManager;
	//Properties reference for constants.
	public static final Properties properties = new Properties();
	//Link to external Economy.
	public static Economy econ;
	//Link to external Permissions.
	public static Permission perms;

	/**
	 * Inherited constructor.
	 * @param name - name of the plugin
	 */
	public AlfCore(String name) {
		super(name);
	}

	public AlfCore() {
		super("AlfCore");
	}

	/**
	 * Get AelfCraft's class manager.
	 * @return - the class manager type
	 */
	public AlfClassManager getClassManager() {
		return this.classManager;
	}

	/**
	 * Get AelfCraft's config manager.
	 * @return - the config manager type
	 */
	public ConfigManager getConfigManager() {
		return this.configManager;
	}

	/**
	 * Get AelfCraft's damage manager.
	 * @return - the damage manager type
	 */
	public CharacterDamageManager getDamageManager() {
		return this.damageManager;
	}

	/**
	 * Get AelfCraft's character manager.
	 * @return - the character manager type
	 */
	public CharacterManager getCharacterManager() {
		return this.characterManager;
	}

	/**
	 * Get AelfCraft's party manager.
	 * @return - the party manager type
	 */
	public PartyManager getPartyManager() {
		return this.partyManager;
	}

	/**
	 * Get AelfCraft's skill manager.
	 * @return - the skill manager type
	 */
	public SkillManager getSkillManager() {
		return this.skillManager;
	}

	/**
	 * Get AelfCraft's effect manager.
	 * @return - the effect manager type
	 */
	public EffectManager getEffectManager() {
		return this.effectManager;
	}
	
	/**
	 * Get AelfCraft's death manager.
	 * @return - the death manager type
	 */
	public DeathManager getDeathManager() {
		return this.deathManager;
	}

	/**
	 * Get AelfCraft's skill configs.
	 * @return - the skill configs
	 */
	public SkillConfigManager getSkillConfigs() {
		return this.skillConfigs;
	}

	/**
	 * Define the skill config loadout.
	 * @param config - new skill configuration manager
	 */
	public void setSkillConfigs(SkillConfigManager config) {
		this.skillConfigs = config;
	}

	/**
	 * Define the class manager.
	 * @param alfClassManager - new class manager
	 */
	public void setClassManager(AlfClassManager alfClassManager) {
		this.classManager = alfClassManager;
	}

	/**
	 * Run on disabling of the plugin.
	 */
	@Override
	public void onDisable() {
		if (characterManager != null) {
			this.characterManager.stopTimers();
			this.characterManager.shutdown();
		}

		econ = null;
		super.onDisable();
	}

	/**
	 * Setup the plugin during enabling.
	 */
	public void setup() {
		if ((getServer().getPluginManager().getPlugin("Vault") == null || (! setupPermissions()))) {
			log(Level.WARNING, "ALFCore requires Vault! Please install it before running again.");
			getServer().getPluginManager().disablePlugin(this);
		} else {
			setupEconomy();

			properties.load(this);
			this.configManager = new ConfigManager(this);
			try {
				this.configManager.load();
			} catch (Exception e) {
				e.printStackTrace();
				log(Level.SEVERE, "Critical error encountered while loading. Disabling...");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}

			this.effectManager = new EffectManager(this);
			this.partyManager = new PartyManager(this);
			this.characterManager = new CharacterManager(this);
			this.damageManager = new CharacterDamageManager(this);
			this.skillManager = new SkillManager(this);
			this.deathManager = new DeathManager(this);

			if (! this.configManager.loadManagers()) {
				getPluginLoader().disablePlugin(this);
				log(Level.SEVERE, "Critical error encountered while loading. Disabling...");
				getServer().getPluginManager().disablePlugin(this);
			}
		}
	}

	/**
	 * Handle setup after first stage of enabling.
	 */
	@SuppressWarnings("unused")
	public void postSetup() {
		Player players[] = getServer().getOnlinePlayers();
		for (Player p : players) {
			//TODO Handle inventory checking and class checking.
		}
	}

	/**
	 * Set up the economy hook.
	 */
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp != null)
			econ = (Economy) rsp.getProvider();
		return econ != null;
	}

	/**
	 * Set up the permission hook.
	 * @return - whether the hook was successful
	 */
	private boolean setupPermissions() {
		if (getServer().getPluginManager().getPlugin("Vault") == null)
			return false;
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		if (rsp != null)
			perms = (Permission) rsp.getProvider();
		return perms != null;
	}

	protected void registerCommands() {
		this.getCommandHandler().addCommand(new HelpCommand(this));
	}

	protected void registerEvents() {
		Bukkit.getPluginManager().registerEvents(new AEntityListener(this), this);
		Bukkit.getPluginManager().registerEvents(new APlayerListener(this), this);
	}
}
