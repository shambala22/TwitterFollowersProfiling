# Google App Engine Twitter Followers Profiler

## API Methods

### Authorization (POST)
\[host\]/auth
#### Content:
    { "username": <bbridge username>,
      "password": <bbridge password> }
#### Response:
    { "token": <authorization token> }

### Profiling (GET)
\[host\]/profiling?screen_name=\[twitter screen name\]
#### Header:
Authorization - \[authorization token\]
#### Response: 
    [{ "id": [twitter user id],
       "profiling": [bbridge profiling](if timeline is public) }]

## Running locally
    $ mvn appengine:devserver

## Deploying
    $ mvn appengine:update
