+ Recorder {
	prRecord { |bus, node, dur|
		recordNode = Synth.tail(node ? RootNode(server), synthDef.name, [\bufnum, recordBuf, \in, bus, \duration, dur ? -1]);
		recordNode.register(true);
		recordNode.onFree { this.stopRecording };
		if(responder.isNil) {
			responder = OSCFunc({ |msg|
				if(msg[2] == id) {
					duration = msg[3];
					this.changedServer(\recordingDuration, duration);
				}
			}, '/recordingDuration', server.addr);
		} {
			responder.enable;
		};
	}

	prRecordJT { |bus, node, dur|
		//recordNode = Synth.after(node, synthDef.name, [\bufnum, recordBuf, \in, bus, \duration, dur ? -1]);
		recordNode = Synth(synthDef.name, [\bufnum, recordBuf, \in, bus, \duration, dur ? -1], node, if (node.class==Group, {\addToTail},{\addAfter}));
		recordNode.register(true);
		recordNode.onFree { this.stopRecording };
		if(responder.isNil) {
			responder = OSCFunc({ |msg|
				if(msg[2] == id) {
					duration = msg[3];
					this.changedServer(\recordingDuration, duration);
				}
			}, '/recordingDuration', server.addr);
		} {
			responder.enable;
		};
	}

	recordJT { |path, bus, numChannels, node, duration|

		server.ifNotRunning { ^this };
		bus = (bus ? 0).asControlInput;

		if(recordBuf.isNil) {
			fork {
				this.prepareForRecord(path, numChannels);
				server.sync;
				this.record(path, bus, numChannels, node, duration) // now we are ready
			}
		} {
			if(numChannels.notNil and: { numChannels != this.numChannels }) {
				"Cannot change recording number of channels while running".warn;
				^this
			};
			if(path.notNil and: { path.standardizePath != this.path }) {
				"Recording was prepared already with a different path: %\n"
				"Tried with this path: %\n".format(this.path, path.standardizePath).error;
				^this
			};
			if(this.isRecording.not) {
				this.prRecordJT(bus, node, duration);
				this.changedServer(\recording, true);
				"Recording channels % ... \npath: '%'\n"
				.postf(bus + (0..this.numChannels - 1), recordBuf.path);
			} {
				if(paused) {
					this.resumeRecording
				} {
					"Recording already (% seconds)".format(this.duration).warn
				}
			}
		}
	}



}