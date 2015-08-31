# jyre-standalone-benchmark

This project comprises a benchmark test for jyre. *Currently, it appears
that messages are being lost when they are sent at high volumes.*

This test runs in a single process and does the following:

* A responder thread sends N "shout" messages to R responders
* each responder sends a unicast "whisper" response back to the responder
* the responder counts up the received messages and reports on the percentage
  received (out of the total expected)

This test can be run from the IDE or from the command line

## Running from the IDE

Run the java class: src/test/java/ManualTest.java

## Running from the command line

To run from the command line, first build the binary distribution

### Building the binary distribution

run the following command

    ./gradlew clean distZip

then retrieve the zip from the directory

    build/distributions

### Run from the command line

First, Unzip the distribution zip
then, cd to the directory where the files are unpacked and run 
the following command to learn the command line args:

    > bin/jyre-standalone-benchmark --help
    usage: jyre-standalone-benchmark
     -i,--interval <arg>        ms to wait between sends
     -m,--numMsgs <arg>         number of messages to send
     -r,--numResponders <arg>   number of responder threads to start

## A command that succeeds

On my linux machine, the following succeeds:

    > bin/jyre-standalone-benchmark --numResponders 10 --numMsgs 10 --interval 100
    
You can tell it succeeds because the console output reports that it received 
100% of expected messages:
    
    INFO  org.test.ZyreRequester - sent: 10 expected: 100 received: 100 (100%)
    
## A command that fails

When the interval between messages decreases, messages appear to get lost.
On my linux machine, the following benchmark fails to deliver all messages:

    > bin/jyre-standalone-benchmark --numResponders 10 --numMsgs 100 --interval 5
    
The output of this command usually says something like:

    INFO  org.test.ZyreRequester - sent: 1000 expected: 10000 received: 292 (3%)


