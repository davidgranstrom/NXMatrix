NXMatrix
========

A matrix mixer. See the docs for further details.

Installation
------------

Clone this repo into your `Platform.userExtensionDir` (e.g. `~/Library/Application\ Support/SuperCollider/Extensions` on OS X) and recompile.

Example usage
-------------

    s.waitForBoot {
        // create a 4x4 matrix mixer 
        m = NXMatrix(4, 4);

        // something to work with
        x = play {
            // read output from matrix mixer (feedback) 
            var matrixOutputs = m.outputChannels.collect {|bus| InFeedback.ar(bus, bus.numChannels) };
            // connect a SinOsc to each matrix input, PM modulation with matrix output
            m.inputChannels.do {|bus, i|
                var sig = SinOsc.ar(rrand(220.0, 880.0), matrixOutputs[i]);
                Out.ar(bus, sig.dup);
            };
        };

        // NOTE: we won't hear any sound at this point, since all routing is internal to the matrix mixer

        // we can create a mixer synth which routes the output to the DAC
        SynthDef(\nx_mixer, {|amp=0.2, out|
            var o = m.outputChannels.collect {|bus| In.ar(bus, bus.numChannels) };
            Out.ar(out, amp * Limiter.ar(o.sum));
        }).add;
    };            
    )

    // start the mixer defined above
    Synth.tail(s, \nx_mixer);

    // play with some knobs
    m.gui;
