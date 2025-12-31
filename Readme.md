# Clockify2Jira

This tool lets you conveniently copy 
all logged entries in a clockify workspace project
to a Jira installation

## Usage

* Copy with specific time frame
``java -jar clockify2jira.jar --start=2025-12-01 --end=2025-12-31``
* Copy with specific time frame and group entries per week
``java -jar clockify2jira.jar --start=2025-12-01 --end=2025-12-31 --groupBy=week``
* Copy default 7 days in the past
  ``java -jar clockify2jira.jar``
* Do not actually copy, just log the entries
  ``java -jar clockify2jira.jar --dry-run``

## Configuration

### Required environment

```
CLOCKIFY_WORKSPACE=         # find out by executing api/clockify.http:2
CLOCKIFY_USER=              # find out by executing api/clockify.http:8
CLOCKIFY_PROJECT=           # check also the output of previous requests
CLOCKIFY_API_TOKEN=         # generate on clockfiy account settings
JIRA_ACCOUNT_ID=            # the identifier from your jira account page
JIRA_API_EMAIL=             # the email you use for login
JIRA_API_TOKEN=             # general token works. Granular needs read/write worklog
JIRA_BASE_URL=              # url to jira instance
```
