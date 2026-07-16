package com.campusconnect.app.profile.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for POST /api/profiles/me/skills/.
 * Exactly one of skillId/skillName should be set — Gson omits null fields,
 * so only the one that's set gets sent:
 *   - skillId:   attach an existing catalog skill (GET /api/profiles/skills/)
 *   - skillName: create + attach a new custom skill (server auto-creates it
 *                with is_predefined=false if the name doesn't already exist)
 */
public class SkillRequest {

    @SerializedName("skill_id")
    public Integer skillId;

    @SerializedName("skill_name")
    public String skillName;

    @SerializedName("proficiency")
    public String proficiency;

    private SkillRequest(Integer skillId, String skillName, String proficiency) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.proficiency = proficiency;
    }

    public static SkillRequest byId(int skillId, String proficiency) {
        return new SkillRequest(skillId, null, proficiency);
    }

    public static SkillRequest byName(String skillName, String proficiency) {
        return new SkillRequest(null, skillName, proficiency);
    }
}
