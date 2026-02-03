package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Scoreboard {
    private final Map<String, ScoreObjective> scoreObjectives = Maps.newHashMap();
    private final Map<IScoreObjectiveCriteria, List<ScoreObjective>> scoreObjectiveCriterias = Maps.newHashMap();
    private final Map<String, Map<ScoreObjective, Score>> entitiesScoreObjectives = Maps.newHashMap();

    /**
     * Index 0 is tab menu, 1 is sidebar, and 2 is below name
     */
    private final ScoreObjective[] objectiveDisplaySlots = new ScoreObjective[19];
    private final Map<String, ScorePlayerTeam> teams = Maps.newHashMap();
    private final Map<String, ScorePlayerTeam> teamMemberships = Maps.newHashMap();
    private static String[] field_178823_g;

    /**
     * Returns a ScoreObjective for the objective name
     */
    public ScoreObjective getObjective(String name) {
        return this.scoreObjectives.get(name);
    }

    /**
     * Create and returns the score objective for the given name and ScoreCriteria
     */
    public ScoreObjective addScoreObjective(String name, IScoreObjectiveCriteria criteria) {
        if (16 < name.length()) {
            throw new IllegalArgumentException("The objective name '" + name + "' is too long!");
        } else {
            ScoreObjective scoreobjective = this.getObjective(name);

            if (null != scoreobjective) {
                throw new IllegalArgumentException("An objective with the name '" + name + "' already exists!");
            } else {
                scoreobjective = new ScoreObjective(this, name, criteria);
                List<ScoreObjective> list = this.scoreObjectiveCriterias.get(criteria);

                if (null == list) {
                    list = Lists.newArrayList();
                    this.scoreObjectiveCriterias.put(criteria, list);
                }

                list.add(scoreobjective);
                this.scoreObjectives.put(name, scoreobjective);
                this.onScoreObjectiveAdded(scoreobjective);
                return scoreobjective;
            }
        }
    }

    public Collection<ScoreObjective> getObjectivesFromCriteria(IScoreObjectiveCriteria criteria) {
        Collection<ScoreObjective> collection = this.scoreObjectiveCriterias.get(criteria);
        return null == collection ? Lists.newArrayList() : Lists.newArrayList(collection);
    }

    /**
     * Returns if the entity has the given ScoreObjective
     */
    public boolean entityHasObjective(String name, ScoreObjective p_178819_2_) {
        Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(name);

        if (null == map) {
            return false;
        } else {
            Score score = map.get(p_178819_2_);
            return null != score;
        }
    }

    /**
     * Returns the value of the given objective for the given entity name
     */
    public Score getValueFromObjective(String name, ScoreObjective objective) {
        if (40 < name.length()) {
            throw new IllegalArgumentException("The player name '" + name + "' is too long!");
        } else {
            Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(name);

            if (null == map) {
                map = Maps.newHashMap();
                this.entitiesScoreObjectives.put(name, map);
            }

            Score score = map.get(objective);

            if (null == score) {
                score = new Score(this, objective, name);
                map.put(objective, score);
            }

            return score;
        }
    }

    public Collection<Score> getSortedScores(ScoreObjective objective) {
        List<Score> list = Lists.newArrayList();

        for (Map<ScoreObjective, Score> map : this.entitiesScoreObjectives.values()) {
            Score score = map.get(objective);

            if (null != score) {
                list.add(score);
            }
        }

        Collections.sort(list, Score.scoreComparator);
        return list;
    }

    public Collection<ScoreObjective> getScoreObjectives() {
        return this.scoreObjectives.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.entitiesScoreObjectives.keySet();
    }

    /**
     * Remove the given ScoreObjective for the given Entity name.
     */
    public void removeObjectiveFromEntity(String name, ScoreObjective objective) {
        if (null == objective) {
            Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.remove(name);

            if (null != map) {
                this.func_96516_a(name);
            }
        } else {
            Map<ScoreObjective, Score> map2 = this.entitiesScoreObjectives.get(name);

            if (null != map2) {
                Score score = map2.remove(objective);

                if (1 > map2.size()) {
                    Map<ScoreObjective, Score> map1 = this.entitiesScoreObjectives.remove(name);

                    if (null != map1) {
                        this.func_96516_a(name);
                    }
                } else if (null != score) {
                    this.func_178820_a(name, objective);
                }
            }
        }
    }

    public Collection<Score> getScores() {
        Collection<Map<ScoreObjective, Score>> collection = this.entitiesScoreObjectives.values();
        List<Score> list = Lists.newArrayList();

        for (Map<ScoreObjective, Score> map : collection) {
            list.addAll(map.values());
        }

        return list;
    }

    public Map<ScoreObjective, Score> getObjectivesForEntity(String name) {
        Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(name);

        if (null == map) {
            map = Maps.newHashMap();
        }

        return map;
    }

    public void removeObjective(ScoreObjective p_96519_1_) {
        this.scoreObjectives.remove(p_96519_1_.getName());

        for (int i = 0; 19 > i; ++i) {
            if (this.getObjectiveInDisplaySlot(i) == p_96519_1_) {
                this.setObjectiveInDisplaySlot(i, null);
            }
        }

        List<ScoreObjective> list = this.scoreObjectiveCriterias.get(p_96519_1_.getCriteria());

        if (null != list) {
            list.remove(p_96519_1_);
        }

        for (Map<ScoreObjective, Score> map : this.entitiesScoreObjectives.values()) {
            map.remove(p_96519_1_);
        }

        this.onScoreObjectiveRemoved(p_96519_1_);
    }

    /**
     * 0 is tab menu, 1 is sidebar, 2 is below name
     */
    public void setObjectiveInDisplaySlot(int p_96530_1_, ScoreObjective p_96530_2_) {
        this.objectiveDisplaySlots[p_96530_1_] = p_96530_2_;
    }

    /**
     * 0 is tab menu, 1 is sidebar, 2 is below name
     */
    public ScoreObjective getObjectiveInDisplaySlot(int p_96539_1_) {
        return this.objectiveDisplaySlots[p_96539_1_];
    }

    /**
     * Retrieve the ScorePlayerTeam instance identified by the passed team name
     */
    public ScorePlayerTeam getTeam(String p_96508_1_) {
        return this.teams.get(p_96508_1_);
    }

    public ScorePlayerTeam createTeam(String name) {
        if (16 < name.length()) {
            throw new IllegalArgumentException("The team name '" + name + "' is too long!");
        } else {
            ScorePlayerTeam scoreplayerteam = this.getTeam(name);

            if (null != scoreplayerteam) {
                throw new IllegalArgumentException("A team with the name '" + name + "' already exists!");
            } else {
                scoreplayerteam = new ScorePlayerTeam(this, name);
                this.teams.put(name, scoreplayerteam);
                this.broadcastTeamCreated(scoreplayerteam);
                return scoreplayerteam;
            }
        }
    }

    /**
     * Removes the team from the scoreboard, updates all player memberships and broadcasts the deletion to all players
     */
    public void removeTeam(ScorePlayerTeam p_96511_1_) {
        this.teams.remove(p_96511_1_.getRegisteredName());

        for (String s : p_96511_1_.getMembershipCollection()) {
            this.teamMemberships.remove(s);
        }

        this.func_96513_c(p_96511_1_);
    }

    /**
     * Adds a player to the given team
     */
    public boolean addPlayerToTeam(String player, String newTeam) {
        if (40 < player.length()) {
            throw new IllegalArgumentException("The player name '" + player + "' is too long!");
        } else if (!this.teams.containsKey(newTeam)) {
            return false;
        } else {
            ScorePlayerTeam scoreplayerteam = this.getTeam(newTeam);

            if (null != getPlayersTeam(player)) {
                this.removePlayerFromTeams(player);
            }

            this.teamMemberships.put(player, scoreplayerteam);
            scoreplayerteam.getMembershipCollection().add(player);
            return true;
        }
    }

    public boolean removePlayerFromTeams(String p_96524_1_) {
        ScorePlayerTeam scoreplayerteam = this.getPlayersTeam(p_96524_1_);

        if (null != scoreplayerteam) {
            this.removePlayerFromTeam(p_96524_1_, scoreplayerteam);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the given username from the given ScorePlayerTeam. If the player is not on the team then an
     * IllegalStateException is thrown.
     */
    public void removePlayerFromTeam(String p_96512_1_, ScorePlayerTeam p_96512_2_) {
        if (this.getPlayersTeam(p_96512_1_) != p_96512_2_) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + p_96512_2_.getRegisteredName() + "'.");
        } else {
            this.teamMemberships.remove(p_96512_1_);
            p_96512_2_.getMembershipCollection().remove(p_96512_1_);
        }
    }

    public Collection<String> getTeamNames() {
        return this.teams.keySet();
    }

    public Collection<ScorePlayerTeam> getTeams() {
        return this.teams.values();
    }

    /**
     * Gets the ScorePlayerTeam object for the given username.
     */
    public ScorePlayerTeam getPlayersTeam(String p_96509_1_) {
        return this.teamMemberships.get(p_96509_1_);
    }

    /**
     * Called when a score objective is added
     */
    public void onScoreObjectiveAdded(ScoreObjective scoreObjectiveIn) {
    }

    public void onObjectiveDisplayNameChanged(ScoreObjective p_96532_1_) {
    }

    public void onScoreObjectiveRemoved(ScoreObjective p_96533_1_) {
    }

    public void func_96536_a(Score p_96536_1_) {
    }

    public void func_96516_a(String p_96516_1_) {
    }

    public void func_178820_a(String p_178820_1_, ScoreObjective p_178820_2_) {
    }

    /**
     * This packet will notify the players that this team is created, and that will register it on the client
     */
    public void broadcastTeamCreated(ScorePlayerTeam playerTeam) {
    }

    /**
     * This packet will notify the players that this team is updated
     */
    public void sendTeamUpdate(ScorePlayerTeam playerTeam) {
    }

    public void func_96513_c(ScorePlayerTeam playerTeam) {
    }

    /**
     * Returns 'list' for 0, 'sidebar' for 1, 'belowName for 2, otherwise null.
     */
    public static String getObjectiveDisplaySlot(int p_96517_0_) {
        switch (p_96517_0_) {
            case 0:
                return "list";

            case 1:
                return "sidebar";

            case 2:
                return "belowName";

            default:
                if (3 <= p_96517_0_ && 18 >= p_96517_0_) {
                    EnumChatFormatting enumchatformatting = EnumChatFormatting.func_175744_a(p_96517_0_ - 3);

                    if (null != enumchatformatting && EnumChatFormatting.RESET != enumchatformatting) {
                        return "sidebar.team." + enumchatformatting.getFriendlyName();
                    }
                }

                return null;
        }
    }

    /**
     * Returns 0 for (case-insensitive) 'list', 1 for 'sidebar', 2 for 'belowName', otherwise -1.
     */
    public static int getObjectiveDisplaySlotNumber(String p_96537_0_) {
        if ("list".equalsIgnoreCase(p_96537_0_)) {
            return 0;
        } else if ("sidebar".equalsIgnoreCase(p_96537_0_)) {
            return 1;
        } else if ("belowName".equalsIgnoreCase(p_96537_0_)) {
            return 2;
        } else {
            if (p_96537_0_.startsWith("sidebar.team.")) {
                String s = p_96537_0_.substring("sidebar.team.".length());
                EnumChatFormatting enumchatformatting = EnumChatFormatting.getValueByName(s);

                if (null != enumchatformatting && 0 <= enumchatformatting.getColorIndex()) {
                    return enumchatformatting.getColorIndex() + 3;
                }
            }

            return -1;
        }
    }

    public static String[] getDisplaySlotStrings() {
        if (null == Scoreboard.field_178823_g) {
            field_178823_g = new String[19];

            for (int i = 0; 19 > i; ++i) {
                field_178823_g[i] = getObjectiveDisplaySlot(i);
            }
        }

        return field_178823_g;
    }

    public void func_181140_a(Entity p_181140_1_) {
        if (null != p_181140_1_ && !(p_181140_1_ instanceof EntityPlayer) && !p_181140_1_.isEntityAlive()) {
            String s = p_181140_1_.getUniqueID().toString();
            this.removeObjectiveFromEntity(s, null);
            this.removePlayerFromTeams(s);
        }
    }
}