package paul.fallen.module;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import paul.fallen.ClientSupport;
import paul.fallen.module.Module.Category;
import paul.fallen.module.modules.client.ClickGuiHack;
import paul.fallen.module.modules.client.FallenLanguage;
import paul.fallen.module.modules.client.Pathfinder;
import paul.fallen.module.modules.client.Tones;
import paul.fallen.module.modules.combat.*;
import paul.fallen.module.modules.movement.*;
import paul.fallen.module.modules.pathing.AutoPilot;
import paul.fallen.module.modules.player.*;
import paul.fallen.module.modules.render.*;
import paul.fallen.module.modules.world.*;
import paul.fallen.utils.client.Logger;
import paul.fallen.utils.client.Logger.LogState;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager implements ClientSupport {

	private final ArrayList<Module> modules = new ArrayList<Module>();

	public ClickGuiHack clickGuiHack;
	public Pathfinder pathfinder;

	public ModuleManager() {
		MinecraftForge.EVENT_BUS.register(this);

		Logger.log(LogState.Normal, "Adding modules to ModuleManager");
		addModule(new HUD(0, "HUD", Category.Render));
		addModule(new AntiSwing(0, "AntiSwing", "AntiSwing", Category.Combat));

		addModule(new AutoPilot(0, "AutoPilot", "AutoPilot", Category.Pathing));

		addModule(new FallenLanguage(0, "FallenLanguage", "FallenLanguage", Category.Client));
		clickGuiHack = new ClickGuiHack(KeyEvent.VK_P, "ClickGUI", "ClickGUI", Category.Client);
		addModule(clickGuiHack);
		addModule(new Tones(0, "Tones", "Tones", Category.Client));
		pathfinder = new Pathfinder(0, "Pathfinder", "Pathfinder", Category.Client);
		addModule(pathfinder);

		addModule(new AutoSprintHack(0, "AutoSprint", "AutoSprint", Category.Movement));

		addModule(new Discord(0, "Discord", "Discord", Category.Player));

		addModule(new FullbrightHack(0, "Fullbright", "Fullbright", Category.Render));

		addModule(new AntiFog(0, "AntiFog", "AntiFog", Category.World));
		addModule(new AntiWeather(0, "AntiWeather", "AntiWeather", Category.World));
	}

	public ArrayList<Module> getModules() {
		return this.modules;
	}

	public ArrayList<Module> getModulesInCategory(Category category) {
		ArrayList<Module> modules = new ArrayList<>();
		for (Module module : getModules()) {
			if (module.getCategory() == category) {
				modules.add(module);
			}
		}
		return modules;
	}

	public ArrayList<Module> getModulesForArrayList() {
		ArrayList<Module> moduleArrayList = new ArrayList<>();
		for (Module module : getModules()) {
			if (module.toggled) {
				moduleArrayList.add(module);
			}
		}
		return moduleArrayList;
	}

	public Module getModule(String s) {
		for (Module m : this.modules) {
			if (m.getName().equalsIgnoreCase(s)) {
				return m;
			}
		}
		return new Module(0, "Null", Category.World);
	}

	public void addModule(Module m) {
		this.modules.add(m);
	}

	public void loadConfig(Gson gson) {
		for (Module m : this.modules) {
			File file = new File(mc.gameDirectory + File.separator + "Fallen" + File.separator + "modules" + File.separator + m.getName() + ".json");
			try (FileReader reader = new FileReader(file)) {
				Map<String, Object> map = gson.fromJson(reader, new TypeToken<Map<String, Object>>() {
				}.getType());
				m.setBind((int) Math.round((double) map.get("bind")));
				m.setState((boolean) map.get("toggled"));
				Logger.log(LogState.Normal, "Loaded module " + m.getName() + " from Json!");
			} catch (JsonSyntaxException e) {
				Logger.log(LogState.Error, "Json syntax error in ModuleManager.loadConfig()!");
				e.printStackTrace();
			} catch (JsonIOException e) {
				Logger.log(LogState.Error, "Json I/O exception in ModuleManager.loadConfig()!");
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				Logger.log(LogState.Error, "Json file not found exception in ModuleManager.loadConfig()!");
				e.printStackTrace();
			} catch (IOException e1) {
				Logger.log(LogState.Error, "Json I/O exception in ModuleManager.loadConfog()!");
				e1.printStackTrace();
			}
		}
	}

	public void saveConfig(Gson gson) {
		for (Module m : this.modules) {
			File file = new File(mc.gameDirectory + File.separator + "Fallen" + File.separator + "modules" + File.separator + m.getName() + ".json");
			if (!file.exists()) {
				new File(mc.gameDirectory + File.separator + "Fallen" + File.separator + "modules").mkdirs();
				try {
					file.createNewFile();
					Logger.log(LogState.Normal, "Created new Json file: " + file.getName());
				} catch (IOException e) {
					Logger.log(LogState.Error, "File.createNewFile() I/O exception in ModuleManager.saveConfig()!");
				}
			}
			try (FileWriter writer = new FileWriter(file)) {
				Map<String, Object> map = new HashMap<>();
				map.put("name", m.getName());
				map.put("bind", m.getBind());
				map.put("toggled", m.getState());
				gson.toJson(map, writer);
				Logger.log(LogState.Normal, "Wrote Json file!");
			} catch (IOException e) {
				Logger.log(LogState.Error, "I/O exception in writing to Json: " + file.getName());
			}
		}
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent event) {
		if (event.getAction() == GLFW.GLFW_PRESS) { // Check if the key is pressed, not released
			for (Module m : this.modules) {
				if (event.getKey() == m.getBind() && !(mc.screen instanceof ChatScreen)) {
					m.setState(!m.getState());
				}
			}
		}
	}
}