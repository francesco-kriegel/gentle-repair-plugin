package de.tudresden.inf.lat.gentlerepair.plugin;

public enum RepairType {

  CLASSICAL_REPAIR_RANDOM("Modified Classical Repair with Random Choices"),
  CLASSICAL_REPAIR_USER("Modified Classical Repair with User Interaction"),
  MODIFIED_GENTLE_REPAIR_SEMANTIC_RANDOM("Modified Gentle Repair (Semantic Weakening) with Random Choices"),
  MODIFIED_GENTLE_REPAIR_SYNTACTIC_RANDOM("Modified Gentle Repair (Syntactic Weakening) with Random Choices"),
  MODIFIED_GENTLE_REPAIR_SEMANTIC_USER("Modified Gentle Repair (Semantic Weakening) with User Interaction"),
  MODIFIED_GENTLE_REPAIR_SYNTACTIC_USER("Modified Gentle Repair (Syntactic Weakening) with User Interaction");

  private final String description;

  private RepairType(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }

}
