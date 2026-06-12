package com.campusconnect.app.profile.models;

public class UserSkill {
    private int id;
    private Skill skill;
    private String proficiency;

    public int getId() { return id; }
    public Skill getSkill() { return skill; }
    public String getProficiency() { return proficiency; }

    public static class Skill {
        private int id;
        private String name;
        private boolean is_predefined;

        public int getId() { return id; }
        public String getName() { return name; }
        public boolean isPredefined() { return is_predefined; }
    }
}