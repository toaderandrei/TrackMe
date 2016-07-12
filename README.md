##TrackMe

This is an application that tracks the user's path. It is based on the old my tracks to some point.
There is a slight difference in the way the service interacts with the UI and also the way data is saved and stored for the user.

The app will have unit tests to cover most of the parts, minimum 60%.
Unit tests can be run via gradle commands, jacoco is also enabled. 
- For unit tests with coverage one must run the following:
  - *gradle createDebugCoverageReport*

* The use case for the app is that when the app is started the user can start/pause/stop the recording.
   * During running the route will be shown on the map 
* The user will be able to see during navigation a starting flag and an ending flag.
* The user will be able to see its track after it has finished.
