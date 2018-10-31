## Questions:
- What is hosted on xyonix s3 and why? (seems like a jar for maven).
- Why is the parser tied to java8? Is it because of Stanford NLP? And, if so, what are the steps for updating if/when Stanford upgrades to java11?
- What is `git-lfs` doing here? Is this recommended for handling the `glove.model` file? If so, can we use a small sample `glove.model` for tests?
- Can build/install/test be untangled?
- What does log4j do here and why?

## To Do:

- Use javadoc
- Unit tests for methods
- Ability to add DB credentials as CLI options to override config file defaults
- `outputPath` should be created by the program if it doesn't exist
- GloVe models should be hosted on s3 (so we don't have to grab them from Stanford each time). Mini models should be available for testing.
- Need Provenance records for models, libraries, and resource files (to be kept in docs) !!! (i.e, what is from stanford? what is ours? what is generated? what is static?)
- Documentation of overall structure
- Documentation of options and their usage
