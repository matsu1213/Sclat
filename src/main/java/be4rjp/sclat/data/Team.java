package be4rjp.sclat.data;

/**
 *
 * @author Be4rJP
 */
public class Team {
    private int teamid;
    private Color teamcolor;
    private String mapname;
    private int paintcount = 0;
    private int killcount = 0;
    private org.bukkit.scoreboard.Team bt;
    
    public Team(int id){this.teamid = id;}
    
    public int getID(){return this.teamid;}
    
    public Color getTeamColor(){return teamcolor;}
    
    public int getPoint(){return this.paintcount;}
    
    public int getKillCount(){return this.killcount;}
    
    public org.bukkit.scoreboard.Team getTeam(){return this.bt;}
    
    public void addPaintCount(){paintcount++;}
    
    public void subtractPaintCount(){paintcount--;}
    
    public void addKillCount(){killcount++;}
    
    public void setTeamColor(Color color){teamcolor = color;}
    
    public void setTeam(org.bukkit.scoreboard.Team team){this.bt = team;}
}
