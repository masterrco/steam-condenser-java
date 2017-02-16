package com.github.koraktor.steamcondenser.servers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import com.github.koraktor.steamcondenser.exceptions.RCONBanException;
import com.github.koraktor.steamcondenser.exceptions.RCONNoAuthException;
import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.servers.packets.rcon.RCONAuthRequestPacket;
import com.github.koraktor.steamcondenser.servers.packets.rcon.RCONAuthResponse;
import com.github.koraktor.steamcondenser.servers.packets.rcon.RCONExecRequestPacket;
import com.github.koraktor.steamcondenser.servers.packets.rcon.RCONExecResponsePacket;
import com.github.koraktor.steamcondenser.servers.packets.rcon.RCONPacket;
import com.github.koraktor.steamcondenser.servers.packets.rcon.RCONTerminator;
import com.github.koraktor.steamcondenser.servers.sockets.RCONSocket;
import com.github.koraktor.steamcondenser.servers.sockets.SourceSocket;

public class Unity7DaysToDieServer extends GameServer {

	public static final Integer HORDE_DAY_INTERVAL=7;
	public static final Integer HOURS_PER_DAY=24;
	public static final Integer MINUTES_PER_HOUR = 60;
	public static final Integer MINUTES_RESOLUTION=1000;
	public static final Integer TIME_NUM_DIGITS=2;
	public static final Integer NIGHT_START_HOUR=22;  //night always starts at 22:00 as of Alpha 15
	public static final Integer AIRDROP_BASE_HOUR=12; // airdrops always drop relative to noon of day 1 as of Alpha 15
	
	public static final String VAR_NUM_PLAYERS = "numberOfPlayers"; // "CurrentPlayers" in rules packet
	public static final String VAR_MAX_PLAYERS = "maxPlayers"; // "MaxPlayers" in rules packet
	public static final String VAR_HOURS_OF_DAYLIGHT = "DayLightLength";
	public static final String VAR_MINUTES_PER_24H = "DayNightLength";
	public static final String VAR_AIRDROP_INTERVAL = "AirDropFrequency";
    public static final String VAR_AIRDROP_MARKED_ON_MAP = "AirDropMarker";
	public static final String VAR_SERVER_TIME = "CurrentServerTime";
    public static final String VAR_SERVER_NAME = "serverName";  // "GameHost" in rules packet
    public static final String VAR_SERVER_GAME_MODE = "GameMode";
    public static final String VAR_MAP_NAME = "mapName";  // "LevelName" in rules packet
    public static final String VAR_MAP_SEED = "GameName";
    public static final String VAR_PVP_MODE =  "PlayerKillingMode";
    public static final String VAR_FRIENDS_ON_MAP = "ShowFriendPlayerOnMap";
    public static final String VAR_GAME_DIFFICULTY = "GameDifficulty";
    public static final String VAR_ZOMBIE_DIFFICULTY = "EnemyDifficulty";
    public static final String VAR_ZOMBIE_MEMORY = "EnemySenseMemory";
    public static final String VAR_ZOMBIE_RUN_MODE = "ZombiesRun";
    public static final String VAR_ZOMBIE_SPAWN_MODE = "EnemySpawnMode";
    public static final String VAR_MAX_ZOMBIES = "MaxSpawnedZombies";
    public static final String VAR_MAX_ANIMALS = "MaxSpawnedAnimals";
    public static final String VAR_LAND_CLAIM_DECAY_MODE = "LandClaimDecayMode";
    public static final String VAR_LAND_CLAIM_HARDNESS_ONLINE = "LandClaimOnlineDurabilityModifier";
    public static final String VAR_LAND_CLAIM_HARDNESS_OFFLINE = "LandClaimOfflineDurabilityModifier";
	public static final String VAR_LAND_CLAIM_DEADZONE = "LandClaimDeadZone";
	public static final String VAR_LAND_CLAIM_DURATION = "LandClaimExpiryTime";
	public static final String VAR_LAND_CLAIM_RADIUS = "LandClaimSize";
	public static final String VAR_BLOCK_BASE_DURABILITY = "BlockDurabilityModifier";
	public static final String VAR_LOOT_RESPAWN_INTERVAL_DAYS = "LootRespawnDays";
	public static final String VAR_LOOT_ABUNDANCE = "LootAbundance";
	public static final String VAR_DROP_ON_DEATH_MODE = "DropOnDeath";
	public static final String VAR_DROP_ON_QUIT_MODE = "DropOnQuit";
	public static final String VAR_IS_PASSWORD_PROTECTED = "IsPasswordProtected";
	public static final String VAR_IS_DEDICATED = "IsDedicated";
	public static final String VAR_ANTI_CHEAT_ENABLED = "EACEnabled";
	public static final String VAR_SERVER_DESCRIPTION = "ServerDescription";
	public static final String VAR_SERVER_WEB_URL = "ServerWebsiteURL";
	public static final String VAR_SERVER_IS_64BIT = "Architecture64";
	public static final String VAR_SERVER_OS_PLATFORM = "Platform";
	
	private Integer realMinutesPer24Hours = 50;//default to the games defaults
	private Integer airDropIntervalHours = 72; // can vary by server settings
	private Integer dayLightLength = 18;
	private Integer firstHourOfLight = 4;

	
    /**
     * Creates a new instance of a server object representing a 7 Days To Die server
     *
     *
     * @param address Either an IP address, a DNS name or one of them
     *        combined with the port number. If a port number is given, e.g.
     *        'server.example.com:27016' it will override the second argument.
     * @throws SteamCondenserException if initializing the socket fails
     */
    public Unity7DaysToDieServer(String address) throws SteamCondenserException, UnknownHostException {
        super(address, 26901);
    }

    /**
     * Creates a new instance of a server object representing a 7 Days To Die server,
     *
     *
     * @param address Either an IP address, a DNS name or one of them
     *        combined with the port number. If a port number is given, e.g.
     *        'server.example.com:27016' it will override the second argument.
     * @param port The port the server is listening on
     * @throws SteamCondenserException if initializing the socket fails
     */
    public Unity7DaysToDieServer(String address, Integer port)
            throws SteamCondenserException, UnknownHostException {
        super(address, port+1);
    }

    /**
     * Creates a new instance of a server object representing a 7 Days To Die,
     *
     *
     * @param address Either an IP address, a DNS name or one of them
     *        combined with the port number. If a port number is given, e.g.
     *        'server.example.com:27016' it will override the second argument.
     * @throws SteamCondenserException if initializing the socket fails
     */
    public Unity7DaysToDieServer(InetAddress address) throws SteamCondenserException, UnknownHostException {
        super(address, 26901);
    }

    /**
     * Creates a new instance of a server object representing a 7 Days To Die server,
     *
     *
     * @param address Either an IP address, a DNS name or one of them
     *        combined with the port number. If a port number is given, e.g.
     *        'server.example.com:27016' it will override the second argument.
     * @param port The port the server is listening on
     * @throws SteamCondenserException if initializing the socket fails
     */
    public Unity7DaysToDieServer(InetAddress address, Integer port)
            throws SteamCondenserException, UnknownHostException {
        super(address, port+1);
    }

    public void initialize()
            throws SteamCondenserException, TimeoutException {
        this.updatePing();
        this.updateServerInfo();
        this.updateChallengeNumber();
        this.updateRules();
        
        this.realMinutesPer24Hours = updateServerTickRate();
        this.dayLightLength = updateHoursOfDaylight();
        this.airDropIntervalHours = Integer.parseInt(updateAirDropFrequency());
        this.firstHourOfLight = this.dayLightLength - (HOURS_PER_DAY - NIGHT_START_HOUR);
        
    }
    /**
     * Disconnects the TCP-based channel used for RCON commands
     *
     * @see RCONSocket#close
     */
    public void disconnect() {
        super.disconnect();
    }

    /**
     * Initializes the sockets to communicate with the Source server
     *
     * @see RCONSocket
     * @see SourceSocket
     */
    public void initSocket() throws SteamCondenserException {
        this.socket = new SourceSocket(this.ipAddress, this.port);
    }
    public void updateStatus()  throws SteamCondenserException, TimeoutException
    {
        this.updateServerInfo();
        this.updateRules();
        return;
    }
    private Integer updateServerTickRate()
    {
        Integer rate = Integer.parseInt(this.rulesHash.get(VAR_MINUTES_PER_24H));
        return rate;
    }
    public Integer reportServerTickRate()
    {
        return  updateServerTickRate();
    }
    public Integer getServerTickRate() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return  updateServerTickRate();
    }
    private Integer updateHoursOfDaylight()
    {
        Integer hours = Integer.parseInt(this.rulesHash.get(VAR_HOURS_OF_DAYLIGHT));
        return hours;
    }
    public Integer reportHoursOfDaylight()
    {
        return  updateHoursOfDaylight();
    }
    public Integer getHoursOfDaylight() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return  updateHoursOfDaylight();
    }
    private String updateServerName()
    {
        String hostname = (String) serverInfo.get(VAR_SERVER_NAME);
        return hostname;
    }
    public String getServerName() throws SteamCondenserException, TimeoutException
    {
        this.updateServerInfo();
        return updateServerName();
    }
    public String reportServerName()
    {
        return updateServerName();
    }
    private Integer updateNumPlayers()
    {
        Integer players = ((Byte) serverInfo.get(VAR_NUM_PLAYERS)).intValue();
        return players;
    }
    public Integer reportNumPlayers()
    {
        return updateNumPlayers();
    }
    public Integer getNumPlayers() throws SteamCondenserException, TimeoutException
    {
        this.updateServerInfo();
        return updateNumPlayers();
    }
    private Integer updateMaxPlayers()
    {
        Integer players = ((Byte) serverInfo.get(VAR_MAX_PLAYERS)).intValue();
        return players;
    }
    public Integer getMaxPlayers() throws SteamCondenserException, TimeoutException
    {
        this.updateServerInfo();
        Integer players = updateMaxPlayers();
        return  players;
    }
    public Integer reportMaxPlayers()
    {
        return updateMaxPlayers();
    }
    private String updateMapName()
    {
        String mapname = (String) serverInfo.get(VAR_MAP_NAME);
        return mapname;
    }
    public String getMapName() throws SteamCondenserException, TimeoutException
    {
        this.updateServerInfo();
        String mapname = updateMapName();
        return mapname;
    }
    public String reportMapName()
    {
        return updateMapName();
    }
    public boolean isFull() throws SteamCondenserException, TimeoutException
    {
    	return this.isFull(0);
    }
    public boolean isFull(Integer reservedSlots) throws SteamCondenserException, TimeoutException
    {
    	this.updateServerInfo();
    	Integer players = ((Byte) serverInfo.get(VAR_NUM_PLAYERS)).intValue();
    	Integer maxPlayers = ((Byte) serverInfo.get(VAR_MAX_PLAYERS)).intValue();
    	
    	return (maxPlayers - players) > reservedSlots;
    }
    private String updatePvpMode()
    {
        String mode = rulesHash.get(VAR_PVP_MODE);
        Integer pvp = Integer.parseInt(mode);
        if(pvp == 0)
        {
            mode = "0 - PVE ONLY (NO PLAYER KILLING)";
        }
        else if (pvp == 1)
        {
            mode= "1 - PVP LIMITED (ONLY ALLIES CAN KILL EACH OTHER)";
        }
        else if (pvp == 2)
        {
            mode= "2 - PVP ENABLED (ALLIES CANNOT KILL EACH OTHER)";
        }
        else if (pvp == 3)
        {
            mode= "3 - PVP FULLY ENABLED (EVERYONE CAN KILL ANYONE)";
        }

        return mode;
    }
    public String reportPvpMode()
    {
        return updatePvpMode();
    }
    public String getPvpMode() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updatePvpMode();
    }
    private String updateFriendlyFire()
    {
        String mode = rulesHash.get(VAR_PVP_MODE);
        Integer pvp = Integer.parseInt(mode);
        if(pvp != 2)
        {
            mode = "ON - ALLIES CAN KILL EACH OTHER";
        }
        else
        {
            mode = "OFF - ALLIES CAN NOT KILL EACH OTHER";
        }

        return mode;
    }
    public String reportFriendlyFire()
    {
        return updateFriendlyFire();
    }
    public String getFriendlyFire() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateFriendlyFire();
    }
    private String updateFriendsOnMapMode()
    {
        String mode = rulesHash.get(VAR_FRIENDS_ON_MAP);
        return mode;
    }
    public String reportFriendsOnMapMode()
    {
        return updateFriendsOnMapMode();
    }
    public String getFriendOnMapMode()  throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateFriendsOnMapMode();
    }
    private String updateFriendlyFireMode()
    {
        String mode = rulesHash.get(VAR_FRIENDS_ON_MAP);
        return mode;
    }
    public String reportFriendlyFireMode()
    {
        return updateFriendlyFireMode();
    }
    public String getFriendlyFireMode()  throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateFriendlyFireMode();
    }

    private String updateGameDifficulty()
    {
        String difficulty = rulesHash.get(VAR_GAME_DIFFICULTY);
        return difficulty;
    }
    public String reportGameDifficulty()
    {
        return updateGameDifficulty();
    }
    public String getGameDifficulty() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateGameDifficulty();
    }
    private String updateZombieDifficulty()
    {
        String difficulty = rulesHash.get(VAR_ZOMBIE_DIFFICULTY);
        return difficulty;
    }
    public String reportZombieDifficulty()
    {
        return updateGameDifficulty();
    }
    public String getZombieDifficulty() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateGameDifficulty();
    }
    private String updateZombieMemory()
    {
        String memory = rulesHash.get(VAR_ZOMBIE_MEMORY);
        return memory;
    }
    public String reportZombieMemory()
    {
        return updateZombieMemory();
    }
    public String getZombieMemory() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateZombieMemory();
    }
    private String updateZombieRunMode()
    {
        String mode = rulesHash.get(VAR_ZOMBIE_RUN_MODE);
        return mode;
    }
    public String reportZombieRunMode()
    {
        return updateZombieRunMode();
    }
    public String getZombieRunMode() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateZombieRunMode();
    }
    private String updateZombieSpawnMode()
    {
        String mode = rulesHash.get(VAR_ZOMBIE_SPAWN_MODE);
        return mode;
    }
    public String reportZombieSpawnMode()
    {
        return updateZombieSpawnMode();
    }
    public String getZombieSpawnMode()  throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateZombieSpawnMode();
    }
    private Integer updateMaxZombies()
    {
        Integer max = Integer.parseInt(rulesHash.get(VAR_MAX_ZOMBIES));
        return max;
    }
    public Integer reportMaxZombies()
    {
        return updateMaxZombies();
    }
    public Integer getMaxZombies()  throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateMaxZombies();
    }
    private Integer updateMaxAnimals()
    {
        Integer max = Integer.parseInt(rulesHash.get(VAR_MAX_ANIMALS));
        return max;
    }
    public Integer reportMaxAnimals()
    {
        return updateMaxAnimals();
    }
    public Integer getMaxAnimals() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateMaxAnimals();
    }
    private String updateLandClaimDecayMode()
    {
        String mode = rulesHash.get(VAR_LAND_CLAIM_DECAY_MODE);
        return  mode;
    }
    public String reportLandClaimDecayMode()
    {
        return  updateLandClaimDecayMode();
    }
    public String getLandClaimDecayMode() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return  updateLandClaimDecayMode();
    }
    private String updateLandClaimHardnessOnline()
    {
        String online_hardness = rulesHash.get(VAR_LAND_CLAIM_HARDNESS_ONLINE);
        return  online_hardness;
    }
    public String reportLandClaimHardnessOnline()
    {
        return updateLandClaimHardnessOnline();
    }
    public String getLandClaimHardnessOnline() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateLandClaimHardnessOnline();
    }
    private String updateLandClaimHardnessOffline()
    {
        String offline_hardness = rulesHash.get(VAR_LAND_CLAIM_HARDNESS_OFFLINE);
        return  offline_hardness;
    }
    public String reportLandClaimHardnessOffline()
    {
        return updateLandClaimHardnessOffline();
    }
    public String getLandClaimHardnessOffline() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateLandClaimHardnessOffline();
    }
    private String updateLandClaimDeadzone()
    {
        String zone = rulesHash.get(VAR_LAND_CLAIM_DEADZONE);
        return  zone;
    }
    public String reportLandClaimDeadzone()
    {
        return updateLandClaimDeadzone();
    }
    public String getLandClaimDeadzone() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateLandClaimDeadzone();
    }
    private String updateLandClaimDuration()
    {
        String duration = rulesHash.get(VAR_LAND_CLAIM_DURATION);
        return  duration;
    }
    public String reportLandClaimDuration()
    {
        return  updateLandClaimDuration();
    }
    public String getLandClaimDuration() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateLandClaimDuration();
    }
    private String updateLandClaimRadius()
    {
        String radius = rulesHash.get(VAR_LAND_CLAIM_RADIUS);
        return  radius;
    }
    public String reportLandClaimRadius()
    {
        return updateLandClaimRadius();
    }
    public String getLandClaimRadius() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateLandClaimRadius();
    }
    private String updateBlockBaseHardness()
    {
        String hardness = rulesHash.get(VAR_BLOCK_BASE_DURABILITY );
        return  hardness;
    }
    public String reportBlockBaseHardness()
    {
        return updateBlockBaseHardness();
    }

    public String getBlockBaseHardness() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateBlockBaseHardness();
    }
    private Integer updateLootRespawnDays()
    {
        Integer days = Integer.parseInt(rulesHash.get(VAR_LOOT_RESPAWN_INTERVAL_DAYS));
        return  days;
    }
    public Integer reportLootRespawnDays()
    {
        return  updateLootRespawnDays();
    }
    public Integer getLootRespawnDays() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return  updateLootRespawnDays();
    }
    private String updateLootAbundance()
    {
        String abundance = rulesHash.get(VAR_LOOT_ABUNDANCE);
        return abundance;
    }
    public String reportLootAbundance()
    {
        return updateLootAbundance();
    }
    public String getLootAbundance() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateLootAbundance();
    }
    private String updateDropOnDeathMode()
    {
        String mode = rulesHash.get(VAR_DROP_ON_DEATH_MODE);
        return mode;
    }
    public String reportDropOnDeathMode()
    {
        return updateDropOnDeathMode();
    }
    public String getDropOnDeathMode()  throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateDropOnDeathMode();
    }
    private String updateDropOnQuitMode()
    {
        String mode = rulesHash.get(VAR_DROP_ON_QUIT_MODE);
        return mode;
    }
    public String reportDropOnQuitMode()
    {
        return updateDropOnQuitMode();
    }
    public String getDropOnQuitMode()  throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateDropOnQuitMode();
    }
    private String updateIsPasswordProtected()
    {
        String pwmode = rulesHash.get(VAR_IS_PASSWORD_PROTECTED);
        return pwmode;
    }
    public String reportIsPasswordProtected()
    {
        return updateIsPasswordProtected();
    }
    public String getIsPasswordProtected() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateIsPasswordProtected();
    }
    private String updateIsDedicated()
    {
        String mode = rulesHash.get(VAR_IS_DEDICATED);
        return mode;
    }
    public String reportIsDedicated()
    {
        return updateIsDedicated();
    }
    public String getIsDedicated() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateIsDedicated();
    }
    private String updateAntiCheat()
    {
        String mode = rulesHash.get(VAR_ANTI_CHEAT_ENABLED);
        return mode;
    }
    public String reportAntiCheat()
    {
        return updateAntiCheat();
    }
    public String getAntiCheat() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateAntiCheat();
    }
    private String updateServerDescription()
    {
        String server_string = rulesHash.get(VAR_SERVER_DESCRIPTION);
        return server_string;
    }
    public String reportServerDescription()
    {
        return updateServerDescription();
    }
    public String getServerDescription() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateServerDescription();
    }
    private String updateServerURL()
    {
        String server_url = rulesHash.get(VAR_SERVER_WEB_URL);
        return server_url;
    }
    public String reportServerURL()
    {
        return updateServerURL();
    }
    public String getServerURL() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateServerURL();
    }
    private String updateIs64Bit()
    {
        String bits = rulesHash.get(VAR_SERVER_IS_64BIT);
        return bits;
    }
    public String reportIs64Bit()
    {
        return  updateIs64Bit();
    }
    public String getIs64Bit() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return  updateIs64Bit();
    }
    private String updateServerPlatform()
    {
        String platform = rulesHash.get(VAR_SERVER_OS_PLATFORM);
        return platform;
    }
    public String reportServerPlatform()
    {
        return updateServerPlatform();
    }
    public String getServerPlatform() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateServerPlatform();
    }
    private String updateServerGameMode()
    {
        String mode = rulesHash.get(VAR_SERVER_GAME_MODE);
        return mode;
    }
    public String reportServerGameMode()
    {
        return updateServerGameMode();
    }
    public String getServerGameMode() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateServerGameMode();
    }
    private String updateMapSeed()
    {
        String mode = rulesHash.get(VAR_MAP_SEED );
        return mode;
    }
    public String reportMapSeed()
    {
        return updateMapSeed();
    }
    public String getMapSeed() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateMapSeed();
    }
    private String updateAirDropMarked()
    {
        String mode = rulesHash.get(VAR_AIRDROP_MARKED_ON_MAP);
        return mode;
    }
    public String reportAirDropMarked()
    {
        return updateAirDropMarked();
    }
    private String getAirDropMarked() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateAirDropMarked();
    }
    private String updateAirDropFrequency()
    {
        String mode = rulesHash.get(VAR_AIRDROP_INTERVAL);
        return mode;
    }
    public String reportAirDropFrequency()
    {
        return updateAirDropFrequency();
    }
    private String getAirDropFrequency() throws SteamCondenserException, TimeoutException
    {
        this.updateRules();
        return updateAirDropFrequency();
    }
    private Integer updateInGameDay() 
    {
		Double currentServerTime = Double.parseDouble(this.rulesHash.get(VAR_SERVER_TIME));
		Double dHumanTime = (currentServerTime / (HOURS_PER_DAY * MINUTES_RESOLUTION));
		Double dHumanDays = Math.ceil(dHumanTime);
		Integer humanDays = dHumanDays.intValue();
    	return humanDays;
    }
    private Integer updateInGameHours()  
    {
		Double currentServerTime = Double.parseDouble(this.rulesHash.get(VAR_SERVER_TIME));
		Double dhumanHours = ((currentServerTime/MINUTES_RESOLUTION) % HOURS_PER_DAY);
		Integer humanHours = dhumanHours.intValue();;
    	return humanHours;
    }
    private Integer updateInGameMinutes() 
    {
		Double currentServerTime = Double.parseDouble(this.rulesHash.get(VAR_SERVER_TIME));
		Double dHumanMinutes = ((currentServerTime%MINUTES_RESOLUTION) /MINUTES_RESOLUTION)*MINUTES_PER_HOUR;
		Integer humanMinutes = dHumanMinutes.intValue();
    	return humanMinutes;
    }
    public Integer getInGameDay() throws SteamCondenserException, TimeoutException
    {
    	this.updateRules();
    	return this.updateInGameDay();
    }
    public Integer getInGameHours()  throws SteamCondenserException, TimeoutException
    {
    	this.updateRules();
    	return this.updateInGameHours();
    }
    public Integer getInGameMinutes()  throws SteamCondenserException, TimeoutException
    {
    	this.updateRules();
    	return this.updateInGameMinutes();
    }
    public Integer reportInGameDay()
    {
        return this.updateInGameDay();
    }
    public Integer reportInGameHours()
    {
        return this.updateInGameHours();
    }
    public Integer reportInGameMinutes()
    {
        return this.updateInGameMinutes();
    }
    public String getInGameTime() throws SteamCondenserException, TimeoutException
    {
    	// currentServerTime as returned by the server is the total number of hours since the start of Day 1 at 00:00 with the last 3 digits denoting the time since the start of the last hour
    	this.updateRules();
    	Integer humanDays = this.updateInGameDay();
    	Integer humanHours = this.updateInGameHours();
    	Integer humanMinutes = this.updateInGameMinutes();
		return "Day " + humanDays.toString() + " " + String.format("%0" + TIME_NUM_DIGITS + "d", humanHours) + ":" +  String.format("%0" + TIME_NUM_DIGITS + "d", humanMinutes);
    }
    public String reportInGameTime()
    {
        Integer humanDays = this.updateInGameDay();
        Integer humanHours = this.updateInGameHours();
        Integer humanMinutes = this.updateInGameMinutes();
        return "Day " + humanDays.toString() + " " + String.format("%0" + TIME_NUM_DIGITS + "d", humanHours) + ":" +  String.format("%0" + TIME_NUM_DIGITS + "d", humanMinutes);
    }
    public static boolean isHordeDay(Integer i)
    {
    	return (i % HORDE_DAY_INTERVAL) == 0 && i != 0;
    }
    private Integer updateNextInGameHordeDay()
    {
        Integer currentDay = this.updateInGameDay();
        if(isHordeDay(currentDay))
        {
            return currentDay;
        }
        else
        {
            return updateLastInGameHordeDay() + HORDE_DAY_INTERVAL;
        }
    }
    public Integer getNextInGameHordeDay() throws SteamCondenserException, TimeoutException
    {
    	this.updateRules();
    	return  updateNextInGameHordeDay();
    }
    public Integer reportNextInGameHordeDay()
    {
        return updateNextInGameHordeDay();
    }
    private Integer updateLastInGameHordeDay()
    {
    	Integer currentDay = this.updateInGameDay();
    	if(currentDay < HORDE_DAY_INTERVAL)
    	{
    		return 0;
    	}
    	if(isHordeDay(currentDay))
    	{
    		return currentDay;
    	}
    	else
    	{
    		return currentDay - (currentDay % HORDE_DAY_INTERVAL);   
    	}
    }
    public Integer getLastInGameHordeDay() throws SteamCondenserException, TimeoutException
    {
    	this.updateRules();
    	return updateLastInGameHordeDay();
    }
    private boolean isNightTime()
    {
    	Integer currentHour = updateInGameHours();
    	return currentHour >= NIGHT_START_HOUR || currentHour <= firstHourOfLight;
    }
    private Integer calculateMinutesUntilNextHordeDay()
    {
        Integer currentDay = updateInGameDay();
        Integer currentHours = updateInGameHours();
        Integer currentMinutes = updateInGameMinutes();
        Integer nextHordeDay = 0;

        if (isHordeDay(currentDay))
        {
            return 0;
        }
        else
        {
            nextHordeDay = reportNextInGameHordeDay();
            Integer totalDays = (nextHordeDay - currentDay - 1);
            Integer totalHours = ((HOURS_PER_DAY - currentHours - 1));
            Integer totalMinutes = (MINUTES_PER_HOUR - currentMinutes);

            Double totalFractonalDays = (Double.parseDouble(""+totalHours) / Double.parseDouble(""+HOURS_PER_DAY) ) + ( ( Double.parseDouble(""+totalMinutes) / Double.parseDouble(""+MINUTES_PER_HOUR) ) / Double.parseDouble(""+HOURS_PER_DAY));

            Double dRealMinutes = (Double.parseDouble(""+totalDays) *  Double.parseDouble(""+realMinutesPer24Hours)) + (totalFractonalDays * Double.parseDouble(""+realMinutesPer24Hours));
            Double realMinutes = Math.rint(dRealMinutes);
            return realMinutes.intValue();
        }
    }
    public Integer reportMinutesUntilNextHordeDay()
    {
        return calculateMinutesUntilNextHordeDay();
    }

    





}
