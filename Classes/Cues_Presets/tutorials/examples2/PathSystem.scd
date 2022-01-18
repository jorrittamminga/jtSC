(
w=Window("Cue", Rect(700,400,700,600)).front;
w.addFlowLayout(4@4, 0@0);
w.alwaysOnTop_(true);
p=PathNameNumberedManager(thisProcess.nowExecutingPath.dirname++"/cues2/");
p.makeGui(w, 600@400)
)

p.deepFilesPathName