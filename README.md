# snomed-ct-loinc-data

### Team Ownership - Product Owner
Data Team

## Getting Started

Follow these instructions to generate a snomed ct loinc data ORIGIN dataset:

1. Clone the [snomed-ct-loinc-data repository](https://github.com/ikmdev/snomed-ct-loinc-data)

```bash
git clone [Rep URL]
```

2. Change local directory to `snomed-ct-loinc-data\snomed-ct-loinc-origin`

3. Ensure the loinc-data/pom.xml contains the proper tags containing source filename for the files such as:
   <source.zip>, <source.version>, etc.

4. Enter the following command to build the ORIGIN dataset:

```bash
mvn clean install -U "-DMaven.build.cache.enable=false"
```

5. Enter the following command to deploy the ORIGIN dataset to Nexus:

```bash
mvn deploy -f snomed-ct-loinc-origin -DdeployToNexus=true -Dmaven.deploy.skip=true -Dmaven.build.cache.enabled=false -Ptinkarbuild -DrepositoryId=nexus-snapshot
```

6. On Nexus, you will find the artifact at the following maven coordinates:

```bash
<dependency>
  <groupId>dev.ikm.data.snomedctloinc</groupId>
  <artifactId>snomed-ct-loinc-origin</artifactId>
  <version>SnomedCT_LOINCExtension_20250321T120000Z+1.0.0-20250904.194932-1</version>
</dependency>
```
