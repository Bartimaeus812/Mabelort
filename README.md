# GmailLabelSorter
Sorts the Gmail inbox into labels according to saved author files.

It uses a json file with a collection of rules for how it should manipulate the gmail,

Marking
Messages with a certain label, marker, can be added to an author list

Saving
This author list will grow as long as the save_authors flag is set

Selecting
Messages are selected by author and this is filtered by additional queries, q

These are then modified in the following way:
+destination
-marker
-remove, (this is an additional option)

The example below describes the following situation,
Rule 1
mark up to 50 dirty plates,
save their author (the bastard responsible)
select all dirty plates from the kitchen counter by the author list
send this selection to the kitchen sink and ensure they are not both dirty and clean

Rule 2
mark up to 50 clean plates
save their author
select all clean plates from the kitchen sink by the author list
send this selection to the kitchen cabinet and make them not dirty

Rule 3
mark up to 50 scumbags
save who they are, remember this.
select all scumbags who are your neighbor and are on the list
send this selection to the trash

```json
{
    "label_rules": [
        {
            "save_authors": false,
            "marker": "dirty",
            "destination": "kitchen sink",
            "q": "label:kitchen counter",
            "remove": "clean"
        },
        {
            "save_authors": false,
            "marker": "clean",
            "destination": "kitchen cabinet",
            "q": "label:kitchen sink"
        },
        {
            "save_authors": true,
            "marker": "scumbag",
            "destination": "TRASH",
            "q": "label:neighbor"
        }
    ]
}
