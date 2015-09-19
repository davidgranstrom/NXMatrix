NXMatrix {
    var <numRows, <numColumns, <numChannels, lagTime, server;

    var <inputChannels, <outputChannels;
    var <matrix;

    *new {|numRows=4, numColumns=4, numChannels=2, lagTime=0.01, server|
        ^super.newCopyArgs(numRows, numColumns, numChannels, lagTime, server).init;
    }

    init {
        server = server ?? { Server.default };
        inputChannels  = numColumns.collect { Bus.audio(server, numChannels) };
        outputChannels = numRows.collect { Bus.audio(server, numChannels) };

        forkIfNeeded {
            this.makeSynthDefs;
            server.sync;
        };

        // clean up on cmd period
        CmdPeriod.doOnce { inputChannels.do(_.free); outputChannels.do(_.free) };
    }

    play {|target, addAction='addToHead'|
        target  = target ?? { server };
        ^matrix = Synth(\nx_matrix, nil, target, addAction);
    }

    makeSynthDefs {
        SynthDef(\nx_matrix, {
            inputChannels.collect {|inBus, colIdx|
                outputChannels.collect {|outBus, rowIdx|
                    var amp = NamedControl.ar("amp_%_%".format(rowIdx, colIdx), 0, lagTime);
                    var sig = In.ar(inBus, inBus.numChannels);
                    Out.ar(outBus, amp * sig);
                };
            }.flop;
        }).add;
    }

    gui {|embed=false|
        var w, view = View();
        var grid = GridLayout();

        numRows.do {|rowIdx|
            numColumns.do {|colIdx|
                var knob = Knob();
                knob.action = {|val|
                    var amp = \amp.asSpec;
                    matrix.set("amp_%_%".format(rowIdx, colIdx), amp.map(val.value));
                };
                grid.add(knob, rowIdx, colIdx);
            };
        };

        view.layout = grid;

        if(embed.not) {
            w = Window("NXMatrix");
            w.view.layout = HLayout(view);
            ^w.front;
        };
        ^view;
    }

    free {
        // will report server errors if already freed by CmdPeriod
        matrix.free;
        inputChannels.do(_.free);
        outputChannels.do(_.free);
    }
}
