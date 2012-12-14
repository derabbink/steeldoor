# SteelDoor
Lab project for a course on cloud computing I took at TU Delft.

## About
SteelDoor is intended to be a secure cloud storage service, i.e. a cloud storage service that can function without any knowledge of users' files or file meta data. A user can then store encrypted files in a way that nobody unauthorized would ever be able read them.
The key features besides possible encryption are seemingly endless storage for possibly endless files, built-in load balancing and replication.

If you want to know more, there is a document named report.pdf included in this repo, explaining a few more details.

## Technology
The repo is set up as a Maven project with a couple of modules.

The "entry point" to the whole system is the web service that is supposed to handle all interactions. All this can be found in the steeldoor-service module, which is based on [Dropwizard][1].

Originally, the intention was to deploy the system on [AWS][2].

## Status
As with so many other lab projects, there was way too little time to make something genuinely amazing, or like in this case: even something that works at all, be it very primitively.
**In short, this project is far from complete.** The only thing that currently runs are a rather empty servlet container and a handful of unit tests.

The most complete part is the steeldoor-serverfiles module, which implements a large portion of the low-level data structures required for persistent storage.

 [1]: http://dropwizard.codahale.com/
 [2]: http://aws.amazon.com/

