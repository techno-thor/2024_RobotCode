package frc.robot.period;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.Timer;
import frc.molib.buttons.ButtonManager;
import frc.molib.dashboard.Chooser;
import frc.molib.utilities.Console;
import frc.robot.Robot;
import frc.robot.subsystem.Chassis;
import frc.robot.subsystem.Hanger;
import frc.robot.subsystem.Runway;

/** Autonomous Period: Handles prewritten sequences for the Robot to run on its own */
@SuppressWarnings({"unused", "deprecation"})
public class Autonomous {
    private static enum StartingPosition {
        SPEAKER_LEFT("Speaker - Left"),
        SPEAKER_CENTER("Speaker - Center"),
        SPEAKER_RIGHT("Speaker - Right"),
        WALL("Wall");

        private final String label;

        private StartingPosition(String label) { this.label = label; }

        @Override public String toString() { return label; }
    }

    private static enum Sequence {
        DO_NOTHING("Do Nothing") { 
            @Override public void periodic() {
                switch(mStage){
                    case 0:
                        Console.logMsg("Starting Sequence \"" + toString() + "\"");
                        mStage++; break;
                    case 1:
                        Console.logMsg("Sequence Complete");
                        mStage++; break;
                    default:
                        Chassis.disable();
                        Runway.disable();
                        Hanger.disable();
                }
            }},
        TRAVEL("Travel") {
            @Override public void periodic() {
                switch(mStage) {
                    case 0:
                        Console.logMsg("Starting Sequence \"" + toString() + "\"");
                        mStage++; break;
                    case 1:
                        Console.logMsg("Driving forward for 1s...");
                        Chassis.setDrivePower(0.5, 0.5);
                        tmrTimeout.reset();
                        mStage++; break;
                    case 2:
                        if(tmrTimeout.get() > 1.0) mStage++;
                        break;
                    case 3:
                        Console.logMsg("Time reached, stopping drive...");
                        Chassis.disableDrive();
                        mStage++; break;
                    case 4:
                        Console.logMsg("Sequence Complete");
                        mStage++; break;
                    default:
                        Chassis.disable();
                        Runway.disable();
                        Hanger.disable();
                }
            }},
        SPEAKER_SHOT("Speaker Shot") {
            @Override public void periodic() {
                switch(mStage) {
                    case 0:
                        Console.logMsg("Starting Sequence \"" + toString() + "\"");
                        mStage++; break;
                    case 1:
                        Console.logMsg("Enabling Reels...");
                        Runway.enableReels_Speaker();
                        tmrTimeout.reset();
                        mStage++; break;
                    case 2:
                        if(tmrTimeout.get() > 0.25) mStage++;
                        break;
                    case 3:
                        Console.logMsg("Time reached. Firing...");
                        Runway.enableDirector();
                        tmrTimeout.reset();
                        mStage++; break;
                    case 4:
                        if(tmrTimeout.get() > 0.25) mStage++;
                        break;
                    case 5:
                        Console.logMsg("Time reached, stopping Runway...");
                        Runway.disable();
                        mStage++; break;
                    case 6:
                        Console.logMsg("Sequence Complete");
                        mStage++; break;
                    default:
                        Chassis.disable();
                        Runway.disable();
                        Hanger.disable();
                }
            }};

        private final String label;
        private static int mStage = 0;
        private static Timer tmrTimeout = new Timer();
        
        private Sequence(String label) { this.label = label; }

        public void init() { 
            mStage = 0; 
            tmrTimeout.reset(); 
            tmrTimeout.start(); 
        }

        public abstract void periodic();

        @Override public String toString() { return label; }
    }

    //Parent NetworkTable
    private static final NetworkTable tblAutonomous = Robot.tblPeriod.getSubTable("Autonomous");

    //Dashboard Objects
    private static Chooser<StartingPosition> chsStartingPosisiton = new Chooser<StartingPosition>(tblAutonomous, "Starting Position", StartingPosition.WALL);
    private static Chooser<Sequence> chsSequence = new Chooser<Sequence>(tblAutonomous, "Sequence", Sequence.DO_NOTHING);

    //Dashboard selections
    private static StartingPosition mSelectedStartingPosition;
    private static Sequence mSelectedSequence;

    /** Private constructor to prevent individual instances from being created */
    private Autonomous() {}

    /** Runs once when Teleoperated is enabled to read period options from the Dashboard */
    public static void init() {
        //Clear any flags on controls
        ButtonManager.clearFlags();

        mSelectedStartingPosition = chsStartingPosisiton.get();
        mSelectedSequence = chsSequence.get();

        mSelectedSequence.init();
    }

    /** Initialize Dashboard values */
    public static void initDashboard() {
        chsStartingPosisiton.init();
        chsSequence.init();
    }

//////////////////////////////////////////////////
    
    /** Call regularly to update PIDs, interpret sensor input, and apply buffered values to components. */
    public static void periodic() {
        mSelectedSequence.periodic();

        //Update Subsystems
        Chassis.periodic();
		Runway.periodic();
        Hanger.periodic();
    }
}
