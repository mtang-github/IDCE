package us.danny.idce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {
	
	//constants
	private static final String splitString = "\\|\\|\\|";
	private static final String fs = System.getProperty("file.separator");
	private static final String directoryName = "plugins" + fs + "IDCE";
	private static final String databaseName = 
											directoryName + fs + "idceDatabase.txt";
	
	//fields
	private final List<TickingCommandExecutor> commandExecutorList;
	
	public Main() {
		commandExecutorList = new ArrayList<>();
	}

    @Override
    public void onEnable() {
    	this.getCommand("idcereload").setExecutor(
    		new org.bukkit.command.CommandExecutor() {
				@Override
				public boolean onCommand(
					CommandSender sender, 
					Command command, 
					String label, 
					String[] args
				) {
					loadCommands();
					if(sender instanceof Player) {
						Player player = (Player)sender;
						player.sendMessage(
							"reloaded IDCE, num commands = " 
									+ commandExecutorList.size()
						);
					}
					return true;
				}
    		}
    	);
    	if(loadCommands()) {
    		System.out.println("Item Detecting Command Executor enabled");
    	}
    	else {
    		createEmptyDatabase();
    		System.out.println("Item Detecting Command Executor enabled no database");
    		System.out.println("No database was found");
    		System.out.println("Created an empty database file: " + databaseName);
    		System.out.println("Use the separator string" + splitString);
    	}
    	new BukkitRunnable(){
			@Override
			public void run() {
				for(TickingCommandExecutor tce : commandExecutorList) {
					tce.run();
				}
			}
		}.runTaskTimer(this, 0, 1);
    }
    
    //returns false if failed to open database file
    private boolean loadCommands() {
    	commandExecutorList.clear();
    	File databaseFile = new File(databaseName);
    	if(!databaseFile.exists() || databaseFile.isDirectory()) {
    		return false;
    	}
    	Scanner scanner;
		try {
			scanner = new Scanner(new FileInputStream(databaseFile));
		} 
		catch (FileNotFoundException e) {
			System.out.println("IDCE could not load database");
			return false;
		}
    	List<DatabaseEntry> databaseEntryList = new ArrayList<>();
    	DatabaseEntry temp;
    	while(scanner.hasNextLine()) {
    		temp = DatabaseEntry.makeFromLine(scanner.nextLine());
    		if(temp != null) {
    			databaseEntryList.add(temp);
    		}
    	}
    	for(DatabaseEntry entry : databaseEntryList) {
    		commandExecutorList.add(new TickingCommandExecutor(
    			entry.timer,
    			new CommandExecutor(
    				entry.lore,
    				entry.command
    			)
    		));
    	}
    	scanner.close();
    	printNumCommands();
    	return true;
    }
    
    @Override
    public void onDisable() {
    	System.out.println("Item Detecting Command Executor disabled");
    }
    
    private void createEmptyDatabase() {
    	File directory = new File(directoryName);
    	if(!directory.exists()) {
    		try {
    			Files.createDirectories(Paths.get(directoryName));
    		}
    		catch(IOException e) {
    			System.out.println("IDCE failed to create its directory");
    		}
    	}
    	File file = new File(databaseName);
    	if(!file.exists()) {
    		try {
				if(file.createNewFile()) {
					System.out.println("IDCE failed to create empty database file");
				}
			} 
    		catch (IOException e) {
				System.out.println("IDCE failed to create empty database");
			}
    	}
    }
    
    private void printNumCommands() {
    	System.out.println("IDCE command num : " + commandExecutorList.size());
    }
    
    private static class DatabaseEntry {
    	private final int timer;
    	private final String lore;
    	private final String command;
    	
    	private DatabaseEntry(int timer, String lore, String command) {
			this.timer = timer;
			this.lore = lore;
			this.command = command;
		}

		public static DatabaseEntry makeFromLine(String databaseLine) {
    		databaseLine = databaseLine.trim();
    		String[] split = databaseLine.trim().split(splitString);
    		if(split.length != 3) {
    			issueBadLineWarning(databaseLine, "could not split into 3 parts");
    			for(String s : split) {
    				System.out.println(s);
    			}
    			return null;
    		}
    		int timer;
    		try {
    			timer = Integer.parseInt(split[0].trim());
    		}
    		catch (Exception e){
    			issueBadLineWarning(databaseLine, "could not parse first as int");
    			return null;
    		}
    		String lore = split[1].trim();
    		String command = split[2].trim();
    		return new DatabaseEntry(timer, lore, command);
    	}
		
		private static void issueBadLineWarning(String badLine, String warning) {
			System.out.println("IDCE bad line: " + badLine);
			System.out.println(warning);
		}
    }
    
    private static class TickingCommandExecutor {
    	private final Ticker ticker;
    	private final CommandExecutor commandExecutor;
    	
    	public TickingCommandExecutor(int timer, CommandExecutor commandExecutor) {
    		ticker = new Ticker(timer);
    		this.commandExecutor = commandExecutor;
    	}
    	
    	public void run() {
    		if(ticker.isTime()) {
    			commandExecutor.executeCommandsForAllPlayersHoldingCorrectItem();
    		}
    	}
    }
}