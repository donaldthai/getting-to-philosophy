# Getting to Philosophy - Donald Thai

Hey there!

This app is built with the Scala-Java Play Framework.

Some technical notes

- Framework: Play Framework, Scala frontend, Java backend
- Database: H2 In-Memory DB for development with Play eBean plugin for ORM
- Java Version: 8
- JSoup for parseing HTML Docs
- SQL scripts can be found in "conf/evolutions/default"

Other notes:

I noticed that Xfer's site has a slight difference implementation when traversing the first links. Xfer's site actually goes to the next link if current link has been visited. I decided to follow the rules defined in the Getting to Philosophy article. The app actually stops traversing if we come across a link we've seen before to prevent loops.

## Challenge
Your goal is to build a full-stack app that takes a Wikipedia URL as input, and display
the path taken from clicking the first link of each page until you get to the Philosophy
article. This app should have a front-end form that interacts with a back-end API you’ve
built, and it should store the paths taken in a database you’ve designed.

Additional Rules
- The front-end should accept a single Wikipedia URL as input.
- The back-end API should visit the given page and keep going through the first
link (as defined below) on each page until Philosophy is reached.
- The paths taken should be stored in a database with a unique identifier for any
given path.
- The front-end should display the path taken to reach Philosophy, and it should
also display the amount of hops it took to get there.
- Please send the source code of the finished exercise in a GitHub repository that
we can access, and either host the app somewhere or provide instructions on
how we can run the app.

For more information and explanation on how to choose the first link, see below:
http://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy

## Running

Run this using [sbt](http://www.scala-sbt.org/) in the project root directory.

```
sbt run
```

And then go to http://localhost:9000 to see the running web application.

On initial startup, you'll see a message "Database 'default' needs evolution!".

Don't worry! Just click on "Apply this script now!". It'll create the development database for the app.

## Controllers

There are two controllers:

- HomeController
    
    - This routes you to the main homepage
    
- PhilController

    - This does all the heavy lifting for "Getting to Philosophy"

## Views

Views can be found in "app/views". They provide Scala templates that the controller can output to for an HTML page.