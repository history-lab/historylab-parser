package edu.columbia.historylab.ner.evaluation;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.handlers.AnnotationDocumentReader;
import edu.columbia.historylab.ner.main.HLParser;
import edu.columbia.historylab.ner.models.Mention;

public class MentionExtractionEvaluator implements IMentionExtractionEvaluator {
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
    public void evaluate(String configPath, String dataDirectory, int limit, boolean strict,
                         boolean verbose, boolean ignoreNoType) {
        try {
            int fileCounter = 0;
            File[] files = new File(new File(dataDirectory).getAbsolutePath()).listFiles(File::isFile);
            double pAvg = 0.0;
            double rAvg = 0.0;
            DecimalFormat df = new DecimalFormat("#.##");

            HLParser hlparser = HLParser.getInstance(configPath);

            for (File file : files) {
                if (!file.getName().endsWith(".txt")) {
                    continue;
                }
                // Check size limit
                if (fileCounter++ > limit) {
                    continue;
                }
                // Read the annotated document
                AnnotationDocumentReader annoDocumentReader = new AnnotationDocumentReader(new File(file.getAbsolutePath()));
                List<Mention> actualMentions = null;
                try {
                    actualMentions = annoDocumentReader.execute(false, false, true);
                } catch (Exception e) {
                    System.out.println("Problems getting mentions from AnnotationDocumentReader");
                    e.printStackTrace();
                    continue;
                }

                // Run parsing on the data and extract mentions
                String parsedText = annoDocumentReader.getTokenizedText();
                List<Mention> candidateMentions = hlparser.parse(parsedText);
                if (verbose) {
                    System.out.println("--------------------------------------------------------");
                    System.out.println(parsedText);
                    System.out.println("--------------------------------------------------------");
                    for ( Mention m: candidateMentions ) {
                        if (m.getNERType() != NERType.NONE) {
                            System.out.println("(" + m.getNERType() + ")\t"+m.getText());
                        }
                    }
                    if ( ! ignoreNoType ) {
                        for (Mention m : candidateMentions) {
                            if (m.getNERType() == NERType.NONE) {
                                System.out.println("(" + m.getNERType() + ")\t" + m.getText());
                            }
                        }
                    }
                }

                //Ignore cases with no mentions
                if (candidateMentions == null) {
                    continue;
                }

                // Count actual mentions with NERType set
                int actual = 0;
                for (Mention actualMention : actualMentions) {
                    if (actualMention.getNERType() == null) {
                        continue;
                    }
                    actual++;
                }

                // Determine valid extracted mentions
                int tp = 0;
                int fp = 0;
                int candidateMentionCount = 0;
                for (Mention candidateMention : candidateMentions) {
                    boolean validMention = false;

                    // Ignore all mentions with type None if requested
                    if ( ignoreNoType && ( candidateMention.getNERType() == null || candidateMention.getNERType() == NERType.NONE ) ) {
                        continue;
                    }
                    candidateMentionCount++;

                    // check if this mention is valid
                    for (Mention actualMention : actualMentions) {
                        if (actualMention.getNERType() == null) {
                            continue;
                        }
                        if (actualMention.getIndex() == candidateMention.getIndex()
                                && actualMention.getLength() == candidateMention.getLength() )
                        {
                            if ( ! ( strict && actualMention.getNERType() != candidateMention.getNERType() )) {
                                validMention = true;
                                break;
                            }
                        }
                    }
                    if (!validMention) {
                        // only count as false positive if NER type is set since we ignore null mentions
                        if (candidateMention.getNERType() != null) {
                            fp++;
                        }
                    } else {
                        tp++;
                    }
                }

                int fn = actual - tp;

                float p = (float) tp / (float) (tp+fp);
                float r = (float) tp / (float) (tp+fn);
                System.out.println("#### "+file.getName()
                        + "\t actual " + actual
                        + " matched " + tp
                        + " / " + candidateMentionCount
                        + ", precision " + df.format(p)
                        + ", recall " + df.format(r));

                pAvg += p;
                rAvg += r;
            }

            if (fileCounter > limit) fileCounter = limit;
            pAvg = pAvg/fileCounter;
            rAvg = rAvg/fileCounter;
            double f = 2*(pAvg*rAvg)/(pAvg+rAvg);
            System.out.println("\nMention Extraction");
            System.out.println("Avg. precision "+df.format(pAvg)+", avg. recall "+df.format(rAvg)+", f1 "+df.format(f));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
