# CustomComp
Edits a LiveSplit splits (.LSS) file to contain a custom comparison with split times based on Source28's Custom Comp algorithm.

Run the compiled CLASS file in command prompt by navigating to the file's location then typing the command "java CustomComp", followed my the full path name to the splits (.LSS) file. So for example, if I have a two folders, one which contains the CLASS file and one named "Data", which contains a split file called "TestSplits.LSS", the full command would be "java CustomComp ..\Data\TestSplits.LSS". Running this command will produce another splits file with the same name as the original, but with "\_CustomComp" added to the end. So for the purpose of this example, the full file name would be "TestSplits_CustomComp.LSS". Upon opening this file with LiveSplit, a new custom comparison will be available named "Custom Comp", which contaians split times according to Source28's Custom Comp algorithm. Because the results of this algorithm are based on segment golds and your overall PB time, it's important to re-run the program on the updated splits file every time you gold a split or PB.

# Sorce28's Custom Comp algorithm
The algorithm used by this program was developed by Source28 (https://www.twitch.tv/source28) and ensures that the timesave over a comparisom is balanced while the end time remains equal to the runner's PB or some other desired goal time, rather than having certain splits with large opportunity for timesave due to a significant timeloss in the runner's PB, and others which are close to gold and therefore more likely to make the runner lose time during attempts.

The algorithm uses an overall PB time, which may be the runner's actual PB or any other desired time, as well as the segment gold times. It then calculates the total amount of possible timesave over the run by subtracting the runner's SoB from their PB and distributes that timesave by adding it to each segment's gold time such that the ratio of amount of time added to each gold is proportional to the original gold times. This ensures that the segment times of longer splits will always be further from gold, and therefore have more opportunity for timesave, than those of shorter splits.
