<img src="https://wiki.wocommunity.org/xwiki/bin/download/WOL/Home/WebHome/icon_256x256.png" alt="WOLips Icon" width="30%" style="float: right;"/>

# WOLips

WOLips is the Eclipse feature for developing WebObjects applications.

## Downloading WOLips
To download a full WOLips installation, including java development tools, plugin development tools, the latest JRE, and helpful plugins like a decompiler and spotbugs, look for the latest release under org.objectstyle.woproject.wolips.product on the right sidebar.

## Installing WOLips

Installing prebuilt versions of WOLips in existing Eclipse installations can be done the same way any other Eclipse plugin is installed.


1. Add the WOLips update URL to the Available Software Sites list (either through **Eclipse > Preferences > Install/Update > Available Software Sites** or **Help > Install New Software... > Add**)
2. Use the WOLips update URL to install the plugin <a href="https://wocommunity.github.io/wolips/repository/">https://wocommunity.github.io/wolips/repository/</a>




### Building WOLips

#### Prerequisites
* Git
* Latest Java LTS or newer
* A recent version of maven

#### From the command line...

1. Checkout source from Github:

	```bash
	git clone https://github.com/wocommunity/wolips.git
	```

2. Build with maven:
	
	```bash
	cd wolips && mvn clean package
	```

3. There is no step 3!

## Installing the build

A p2 repository is created at wolips/wolips.p2/target/repository/. You can install it as with the install site above, but using the local directory instead of a URL to the remote repository. The version number is timestamped, so you can immediately update your local wolips install after a fresh build if you desire.

## Develop WOLips under Eclipse

### Prepare eclipse:

1) Install the latest "Eclipse IDE for Eclipse Committers" found at <a href="https://www.eclipse.org/downloads/packages/">https://www.eclipse.org/downloads/packages/</a> for your platform. You need the PDE plugins for WOLips development. 

2) In the eGit perspective, add the WOLips github repository

3) Right click on your repository's "Working Tree" and "Import Projects..." to import the WOLips project
   
4) Switch to your plugin development view. You can now build the project with maven for the first time, which generates the necessary woenvironment.jar and woproject-ant-tasks.jar files and the entire project should compile in Eclipse without errors.


### Run WOLips in Eclipse

To run WOLips with your Eclipse installation and all your existing plugins,

1) Open the wolips plugin at wolips/wolips_wolips/core/plugins/org.objectstyle.wolips/plugin.xml
   
2) Click on "Launch an Eclipse application" under the Testing heading. You can also choose to debug here if you want to set breakpoints and debug. Once you do this once, a launcher will be added to your Run/Debug configurations and you can launch from there next time.

### Run the WOLips product in Eclipse:

To run WOLips with a barebones Eclipse application and just the WOLips feature installed

1) Open the wolips product at wolips/wolips_wolips/wolips.product/wolips.product

2) Click on "Launch an Eclipse application" under the Testing heading. You can also choose to debug here if you want to set breakpoints and debug. Once you do this once, a launcher will be added to your Run/Debug configurations and you can launch from there next time.

