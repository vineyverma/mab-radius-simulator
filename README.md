# mab-radius-simulator

Simulator code for simulating Radius and MAB packets.
This is leveraging simulator developed by Alan Lei 
https://github.com/alei121/java-radius-simulator

Alan's simulator is enhanced to perform following:

- Send MAB packet (Access_Request)
- Receive a Custom AV pair for iot project from command line
- Concurrent radius packets
- Concurrent MAB packets
- Generate random mac addresses
- Randomly choose mac addresses from pre-defined list of mac addresses
- Really useful when you want to test radius w/o using Switch and like to use Custom attributes
