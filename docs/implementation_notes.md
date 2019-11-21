## Implementation Notes

General Flow of how a request gets sent to the Branch Servers

1. Initialize Branch
  `BranchSearch searchSDK = BranchSearch.init(getApplicationContext());`
  - Note that a BranchConfiguration can be used to customize results.  A Default configuration will be used if none is passed into init().

2. Create a BranchSearchRequest
  `BranchSearchRequest request = BranchSearchRequest.Create(keyword);`
  - Note that the request can be modified with things like location before it is sent

3. Start the request
  `BranchSearch.getInstance().query(request, callback);`

4. The Payload is calculated as a combination of
  1. `BranchDiscoveryRequest` base class items, specific to all discovery requests.
  2. `BranchSearchRequest` Search specific items
  3. `BranchDeviceInfo` Basic information about the device that doesn't change between requests
  4. `BranchConfiguration` Branch stuff, and also overrides to the `BranchDeviceInfo` such as overriding Locale

5. BranchSearchInterface starts the query
  `BranchSearchInterface.Search(request, branchConfiguration, callback);`

