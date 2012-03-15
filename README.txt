MANIANA TODO LIST MANAGER
-------------------------

Maniana is an open source todo list app for Android. Its source code and issue tracking system
are is hosted here http://code.google.com/p/maniana and it is available on Google Play at
https://play.google.com/store/apps/details?id=com.zapta.apps.maniana

contact: maniana@zapta.com

About This Repository
---------------------
The repository contains two Eclipse projects:

Maniana 
    an Android App project that is used to build Maniana.

ManianaTest
    an Android test project that is used to unit test portion of Maniana
    code (in the Maniana above project).

Typically the top of this repo is mapped as an Eclipse workspace directory such that Maniana and 
ManianaTest projects are top level directories in that eclipse workspace. Explaining how to
set android SDK and eclipse or how to develope android apps is outside the scope of this
document but feel free to send mean email (address above) if you encounter any problem.

For license information please read LICENSE.txt


Top Level Packages
------------------
Maniana jave source code code is under Maniana/src and it contains these top level packages

com.zapta.apps.maniana 
    Maniana main package. This is also the package that identifies this android app globaly.
android.support 
    this is a package that contains android SDK code that was modified 
    for Maniana purposes (as of March 2012, ViewPager view).
other packages
    contains third party source code that was incroperated into Maniana.

Key Maniana Packages
---------------------
Following is a short description of key Maniana packages

com.zapta.apps.maniana.main
    Contains the main activity. This is the activity that contains the two pages.
    It also contains the AppContext class whose instance contains referenes to all
    the parts of the main activity.

com.zapta.apps.maniana.controller
    Contains the controller of the main activity (as in MVC). The controller class
    recives events in the form of method calls and it controls the model 
    and the view. 

com.zapta.apps.maniana.model
    Contains the model portion of the main activity. The model contains the task data. It is
    made of three levels, AppMode, PageModel and ItemModel (tasks are called 'items' in the source
    code).  

com.zapta.apps.maniana.persistence
    Contains the persistence logic of the model. The model is persisted as a JASON 
    file to the android internal storage of the app.

com.zapta.apps.maniana.view
    Contains the view portion of the main activity. The view represent the display of the main
    activity (pages, tasks, buttons, date, etc). The view reflects data changes made
    in the model and reports to the controller UI operations such as item drag and drop,
    button press, item menu selection, etc.

com.zapta.apps.maniana.preferences
    Contains the settings activity and the preferences logic.

com.zapta.apps.maniana.widget
    Contains the logic related to the home screen app widgets

com.zapta.apps.maniana.services
    Provides access to various Android services.

com.zapta.apps.maniana.util
    Various utility classes.

com.zapta.apps.maniana.backup
    Interfaces with the Android backup services.

com.zapta.apps.maniana.editors
    Contains the item (task) editing activity

com.zapta.apps.maniana.help
    Various logic related to the informative popups (help page, what's new, about page, etc)

com.zapta.apps.maniana.quick_action
    Implements the item popup menu.


Data Files
----------
Note: for simplicity we refer below to Manina root directory in the android internal 
storate as $DROOT. On my phone, it is at /data/data/com.zapta.apps.maniana

$DRROT/shared_prefs/com.zapta.apps.maniana_preferences.xml 
    A standard android prefernece file that persists the user preferences. These preferences
    corresponds to the keys in Maniana/res/xml/preferences.xml

$DROOT/files/maniana_data.json
    A JSON file with the persisted model data. This is the main data file of Maniana.

$DROOT/files/list_widget_image_?x?.png
    Bitmap files of list widgets of the corresponding size. They are used becuse android
    RemoteViews class does not allow to set custom fonts (e.g. cursive) in android
    home screen app widgets. These files are updated when the content of widgets 
    changes.

$DROOT/databases/...
    Maniana does not use database explicitly. All data is store in files. These directory
    is manages by Android components used by Maniana (e.g. WebView view which is used
    to show help page and other popup informative messages).


User Action Sequence
--------------------
TODO: describe here the handling of a typical user action from start to finish (e.g. setting an item
as completed).

