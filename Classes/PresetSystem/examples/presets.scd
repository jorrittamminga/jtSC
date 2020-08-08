x=[0,1,2,3,4];
x=x.move(2,4);
[ 0, 1, 3, 4, 2 ]
x=x.move(4,2);





["0000_test0", "0001_test1", "0002_test2"];
["0000_test0", "0002_test2", "0001_test1"];//this.swap(1,2)
["10000_test0", "10001_test2", "10002_test1"];//this.rename 1
["0000_test0", "0001_test2", "0002_test1"];//this.rename 2

["0000", "0001", "0002"];
["0000", "0002", "0001"];//this.swap(1,2)
["0000", "10001", "10002"];//this.rename 1, 2
["0000", "0001", "0002"];

["0000", nil, "0002"];
["0000", "0002", nil];//this.swap(1,2)
["0000", "0001", nil];//this.rename 1
["0000", "0001", nil];

["0000_test0", "0001_test1", "0002_test2"];//this.insert(1, preset)
["0000_test0", "9999", "0001_test1", "0002_test2"];//this.rename
["0000_test0", "0001", "0002_test1", "0003_test2"];//

["0000", "0001", "0002"];//this.insert(1, preset)
["0000", "9999", "0001", "0002"];//this.rename, from 1 to end
["0000", "0001", "0002", "0003"];//


x=[0,1,2,3,4,5,6];
x=x.move(2,5);
[ 0, 1, 3, 4, 5, 2, 6 ]
rename
shiftfiles(2 t/m 4)
rename
x



x=["0000_test0", "0001_test1", "0002_test2"];
x.removeAt(1);
x

(
w=Window.new.front;  w.addFlowLayout; w.alwaysOnTop_(true);
v=ListView(w, 350@100).items_(["0000", "0001", "0002", "0003", "0004"]).action_{|v|
	if (v.selection.size==1, {
		"go".postln;
	});
};
//v.selectionAction={|l| l.selection.postln};
v.selectionMode=\contiguous;
v.mouseUpAction_({|v| v.value.post;v.selection.postln });
)

x=[0,1,2,3,4];
x.move(0,4);
x

