package edu.columbia.historylab.ner.evaluation;

public interface IMentionExtractionEvaluator {
    /**
     * Evaluate mention extraction / entity recognition.
     * Entities with no NER type are ignored for this evaluation.
     *
     * @param dataDirectory Data directory containing tagged files
     * @param limit Maximum number of files to consider from the directory
     * @param strict Compare NER types for successful match or not
     * @param verbose Enable verbose output
     * @param ignoreNoType Ignore entities with no type assignment or type NONE
     */
    public void evaluate(String configPath, String dataDirectory, int limit,
                         boolean strict, boolean verbose, boolean ignoreNoType);

}
