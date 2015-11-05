package cern.c2mon.daq;

import org.apache.commons.cli.*;

/**
 * @author Justin Lewis Salmon
 */
class OptionsParser {
  private Options options = new Options();

  public OptionsParser(String[] args){
    CommandLineParser parser = new DefaultParser();

    options.addOption(Option.builder("p").longOpt("process-name")       .desc("name of the DAQ process (required)")          .hasArg().argName("filename").required().build());
    options.addOption(Option.builder("c").longOpt("local-config")       .desc("use local DAQ configuration file")            .hasArg().argName("filename").build());
    options.addOption(Option.builder("s").longOpt("save-remote-config") .desc("save the remote DAQ configuration to a file") .hasArg().argName("filename").build());
    options.addOption(Option.builder("t").longOpt("test-mode")          .desc("use test mode (no server connections will be made)").build());
    options.addOption(Option.builder("d").longOpt("no-dynamic-deadband").desc("disables all dynamic deadband filtering (static filtering remains active)").build());

    try {
      CommandLine cmd = parser.parse(options, args);
    } catch (ParseException e) {
      help();
    }

    help();
  }

  private void help() {
    HelpFormatter formatter = new HelpFormatter();
//    formatter.printHelp(110, "DaqStartup", "start a C2MON DAQ process", options, "", true);
    formatter.printHelp(110, "daqprocess [-xml] start|stop|restart|status PROCESS_NAME", "start a C2MON DAQ process\n\nif the -xml parameter is specified, " +
        "only XML output will be served\n\n", options, "\ne.g: daqprocess start P_TEST -t -c conf/local/P_TEST.xml", true);
    System.exit(0);
  }
}
