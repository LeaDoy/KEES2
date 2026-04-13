package com.kees2.onboarding;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "neighborhood-onboarding-validator", mixinStandardHelpOptions = true)
public class OnboardingValidatorApp implements Callable<Integer> {

  @Option(names = "--acs-file", required = true, description = "Path to approved-client-specs.yaml")
  Path acsFile;

  @Override
  public Integer call() throws Exception {
    return AcsValidator.validate(acsFile);
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new OnboardingValidatorApp()).execute(args));
  }
}
