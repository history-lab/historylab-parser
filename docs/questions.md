## Questions:
- What is hosted on xyonix s3 and why? (see error #1)
- Why is the parser tied to java8? Is it because of Stanford NLP? And, if so, what are the steps for updating if/when Stanford upgrades to java11?
- What is `git-lfs` doing here? Is this recommended for handling the `glove.model` file? If so, can we use a small sample `glove.model` for tests?

## To Do:

- Use javadoc
- Unit tests for methods
- Option to add DB credentials as CLI options instead of in config file
- `outputPath` should be created by the program
- Glove models should be hosted on s3 (so we don't have to grab them from Stanford each time), and cached by Travis
- Provenance records for models, libraries, and resource files (to be kept in docs)
- Documentation of overall structure
- Documentation of options and their usage

## Errors:

1: [WARNING] Could not transfer metadata tw.edu.ntu.csie:libsvm/maven-metadata.xml from/to xyonix-snapshot (https://s3-us-west-2.amazonaws.com/com.xyonix.repo/snapshot): Access denied to: https://s3-us-west-2.amazonaws.com/com.xyonix.repo/snapshot/tw/edu/ntu/csie/libsvm/maven-metadata.xml , ReasonPhrase:Forbidden
