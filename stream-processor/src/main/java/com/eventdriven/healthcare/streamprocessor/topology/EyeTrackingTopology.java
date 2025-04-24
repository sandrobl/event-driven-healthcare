package com.eventdriven.healthcare.streamprocessor.topology;

import magicalpipelines.model.TranslatedGaze;
import com.eventdriven.healthcare.streamprocessor.partitioner.CustomPartitioner;
import com.eventdriven.healthcare.streamprocessor.serialization.Gaze;
import com.eventdriven.healthcare.streamprocessor.serialization.avro.AvroSerdes;
import com.eventdriven.healthcare.streamprocessor.serialization.json.GazeSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import static com.eventdriven.healthcare.streamprocessor.gazeprocessing.Processor.calculateAverageLeftRightReading;
import static com.eventdriven.healthcare.streamprocessor.gazeprocessing.Processor.findAOI;

public class EyeTrackingTopology {


  public static Topology build() {

    // the builder is used to construct the topology
    StreamsBuilder builder = new StreamsBuilder();

    // start streaming gazes using our custom value serdes.
    KStream<byte[], Gaze> stream =
        builder.stream("gazes", Consumed.with(Serdes.ByteArray(), new GazeSerdes()));
    stream.print(Printed.<byte[], Gaze>toSysOut().withLabel("gazes-stream"));

    // Apply content filter to gazes // Keep only relevant attributes
      KStream<byte[], Gaze> contentFilteredGazes =
              stream.mapValues(
                  (gaze) -> {
                      Gaze contentFilteredGaze = new Gaze();
                      contentFilteredGaze.setTimestamp(gaze.getTimestamp());
                      contentFilteredGaze.setXposLeft(gaze.getXposLeft());
                      contentFilteredGaze.setYposLeft(gaze.getYposLeft());
                      contentFilteredGaze.setXposRight(gaze.getXposRight());
                      contentFilteredGaze.setYposRight(gaze.getYposRight());
                      contentFilteredGaze.setValidGazeLeft(gaze.isValidGazeLeft());
                      contentFilteredGaze.setValidGazeRight(gaze.isValidGazeRight());
                      contentFilteredGaze.setPupilSizeLeft(gaze.getPupilSizeLeft());
                      contentFilteredGaze.setPupilSizeRight(gaze.getPupilSizeRight());
                      contentFilteredGaze.setValidPupilSizeLeft(gaze.isValidPupilSizeLeft());
                      contentFilteredGaze.setValidPupilSizeRight(gaze.isValidPupilSizeRight());
                      return contentFilteredGaze;
                              });


      // Apply event filter to gazes
      // Keep only gazes with valid ValidGazeLeft, ValidGazeRight, ValidPupilSizeLeft, ValidPupilSizeRight
      KStream<byte[], Gaze> eventFilteredGazes =
            contentFilteredGazes.filter(
            (key, gaze) -> {
              return (gaze.isValidGazeLeft() && gaze.isValidGazeRight() && gaze.isValidPupilSizeLeft() && gaze.isValidPupilSizeRight() );
            });

      // Apply event translator
      // compute xpos from xposLeft and xposRight
      // compute ypos from yposLeft and yposRight
      // compute pupilSize from pupilSizeLeft and pupilSizeRight
      // find AOI based on xpos and ypos
      KStream<byte[], TranslatedGaze> eventTranslatedGazes =
              eventFilteredGazes.mapValues(
                  (gaze) -> {

                      double xPos = calculateAverageLeftRightReading(gaze.getXposLeft(),gaze.getXposRight());
                      double yPos = calculateAverageLeftRightReading(gaze.getYposLeft(),gaze.getYposRight());
                      double pupilSize = calculateAverageLeftRightReading(gaze.getPupilSizeLeft(),gaze.getPupilSizeRight());
                      String aoi = findAOI(xPos,yPos);

                      TranslatedGaze translatedGaze =
                              TranslatedGaze.newBuilder()
                                      .setTimestamp(gaze.getTimestamp())
                                      .setXpos(xPos)
                                      .setYpos(yPos)
                                      .setPupilSize(pupilSize)
                                      .setAOI(aoi)
                                      .build();

                      return translatedGaze;
                      });

      // Apply event router
      // For sake of simulation divide gazes into two gazeBranches based on pupilSizeThreshold
      // gazes with pupil size less than pupilSizeThreshold are assumed to reflect low cognitive load, while the other gazes are assumed to reflect high cognitive load
      double pupilSizeThreshold = 3.15;
      KStream<byte[], TranslatedGaze>[] gazeBranches = eventTranslatedGazes.branch(
              (k, gaze) -> gaze.getPupilSize() < pupilSizeThreshold,
              (k, gaze) -> gaze.getPupilSize() >= pupilSizeThreshold);

        // Route gazeBranches to different partitions of the same topic and process them
          for (int i = 0; i < gazeBranches.length; i++) {

              // Select branch
              KStream<byte[], TranslatedGaze> branch = gazeBranches[i];

              // Create a new key based on the partitioning condition
              int fi = i;
              KStream<String, TranslatedGaze> keyedStream = branch.selectKey((key, value) -> {
                  if (fi==0) {
                      return "low CL";
                  } else {
                      return "high CL";
                  }
              });

              // Write to the output topic
                  keyedStream.to(
                          "gazes-out",
                          Produced.with(
                                  Serdes.String(),
                                  AvroSerdes.avroGaze("http://localhost:8081", false),
                                  new CustomPartitioner()));
          }



    return builder.build();
  }
}
