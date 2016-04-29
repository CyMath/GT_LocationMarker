# Green Tech Location Marker

This is a helper app to the [Green Tech App](https://github.com/nepash/Green_Tech) to mark/record the locations of a recycling bin or water refill station on Texas Tech campus. 

###Features:
1. Add Location Markers
2. Remove Location Markers
3. Save Markers to local geoJSON file
4. Upload geoJSON file to Googe Drive

###How to use:
1. Turn on Location
2. Turn on Internet
3. Stand next to Recycling Bin/Water Refill and click pink "Add Marker" button. This will lead you to an entry form where you pick which floor you are on, what type of item you are logging and what building you are in. Once you have finished your entry (double check!!), click Add. 

The locations will be saved locally to a GeoJSON file. 
Because the relability of this app can't be guaranteed, it is recommended that you upload the geoJSON file to your Google Drive every now and then. This can be done using the "Cloud Upload" button found on the app bar at the top by the "Location Marker" title. It won't overwrite your previous file, so if nothing went wrong, the last one uploaded would be your most recent one. This way, you can backup the file to the cloud. 


###Usage Warning:

List of Locations does not update the number of the entry, when deleting, properly till the app is restarted

When uploading to Google Drive, you MUST have internet on, otherwise upload window will hang/freeze. You will then have to close the window and try again

When using app, please remember to have your GPS on. Otherwise, location data will be useless/null.
