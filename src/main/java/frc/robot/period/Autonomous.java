package frc.robot.period;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.molib.buttons.ButtonManager;
import frc.molib.dashboard.Chooser;
import frc.molib.utilities.Console;
import frc.robot.Robot;
import frc.robot.subsystem.Chassis;
import frc.robot.subsystem.Hanger;
import frc.robot.subsystem.Runway;

/** Autonomous Period: Handles prewritten sequences for the Robot to run on its own */
@SuppressWarnings({"deprecation"})
public class Autonomous {
    /** Where on the field the Robot starts the match, <i>relative to other field elements.</i> */
    private static enum StartingPosition {
        SPEAKER_STAGE("Speaker: Stage"),
        SPEAKER_CENTER("Speaker: Center"),
        SPEAKER_WALL("Speaker: Wall"),
        STATION_WALL("Driver Station Wall"),
        AMPLIFIER("Amplifier");

        private final String label;

        private StartingPosition(String label) { this.label = label; }

        @Override public String toString() { return label; }
    }

    /** The first step in each Sequence will be an optional delay. To have no delay, use {@link #ZERO} */
    private static enum StartDelay {
        ZERO(0.0),
        FIVE(5.0),
        TEN(10.0);

        public final double time;

        private StartDelay(double time) { this.time = time; }
    }

    private static enum Sequence {
        /** Do absolutely nothing */
        DO_NOTHING("Do Nothing") { 
            @Override public void periodic() {
                switch(mStage){
                    case 0:
                        Console.logMsg("Starting Sequence \"" + toString() + "\"");
                        mStage++; break;
                    case 1: 
                        if(tmrTimeout.get() > mSelectedStartDelay.time) mStage++;
                        break;
                    case 2:
                        Console.logMsg("Sequence Complete");
                        mStage++; break;
                    default:
                        Robot.disableSubsystems();
                }
            }},
        /** Just cross the line. <i>Designed only for {@link StartingPosition#STATION_WALL}.  */
        TRAVEL("Travel Only") {
            @Override public void periodic() {
                switch(mStage) {
                    case 0:
                        Console.logMsg("Starting Sequence \"" + toString() + "\"");
                        mStage++; break;
                    case 1: 
                        if(tmrTimeout.get() > mSelectedStartDelay.time) mStage++;
                        break;
                    case 2:
                        Console.logMsg("Driving forward 8ft...");
                        Chassis.resetDistance();
                        Chassis.goToDistance(96.0);
                        tmrTimeout.reset();
                        mStage++; break;
                    case 3:
                        if(Chassis.isAtDistance() || tmrTimeout.get() > 2.0) mStage++;
                        break;
                    case 4:
                        Console.logMsg("Distance reached, stopping drive...");
                        Chassis.disable();
                        mStage++; break;
                    case 5:
                        Console.logMsg("Sequence Complete");
                        mStage++; break;
                    default:
                        Robot.disableSubsystems();
                }
            }},
        /** Cross the field, ready to go to the Source. <i>Designed only for {@link StartingPosition#STATION_WALL}.</i> */
        CROSS_FIELD("Cross Field Only") {
            @Override public void periodic() {
                switch(mStage) {
                    case 0:
                        Console.logMsg("Starting Sequence \"" + toString() + "\"");
                        mStage++; break;
                    case 1: 
                        if(tmrTimeout.get() > mSelectedStartDelay.time) mStage++;
                        break;
                    case 2:
                        Console.logMsg("Driving forward 20ft...");
                        Chassis.resetDistance();
                        Chassis.goToDistance(240.0);
                        tmrTimeout.reset();
                        mStage++; break;
                    case 3:
                        if(Chassis.isAtDistance() || tmrTimeout.get() > 4.0) mStage++;
                        break;
                    case 4:
                        Console.logMsg("Distance reached, stopping drive...");
                        Chassis.disable();
                        mStage++; break;
                    case 5:
                        Console.logMsg("Sequence Complete");
                        mStage++; break;
                    default:
                        Robot.disableSubsystems();
                }
            }},
        /** Shoot into the Speaker and do nothing else. */
        SPEAKER_SHOT_STAY("Speaker Shot & Stay") {
            @Override public void periodic() {
                switch(mStage) {
                    case 0:
                        Console.logMsg("Starting Sequence \"" + toString() + "\"");
                        mStage++; break;
                    case 1: 
                        if(tmrTimeout.get() > mSelectedStartDelay.time) mStage++;
                        break;
                    case 2:
                        Console.logMsg("Enabling Reels...");
                        Runway.enableReels_Speaker();
                        tmrTimeout.reset();
                        mStage++; break;
                    case 3:
                        if(tmrTimeout.get() > 0.25) mStage++;
                        break;
                    case 4:
                        Console.logMsg("Time reached. Firing...");
                        Runway.enableDirector();
                        tmrTimeout.reset();
                        mStage++; break;
                    case 5:
                        if(tmrTimeout.get() > 0.25) mStage++;
                        break;
                    case 6:
                        Console.logMsg("Time reached, stopping Runway...");
                        Runway.disable();
                        mStage++; break;
                    case 7:
                        Console.logMsg("Sequence Complete");
                        mStage++; break;
                    default:
                        Robot.disableSubsystems();
                }
            }},
        /** Shoot into the Speaker, turn around, and drive forward just enough to cross the Travel line. */
        SPEAKER_SHOT_TRAVEL("Speaker Shot & Travel") {
            @Override public void periodic() {
                switch(mSelectedStartingPosition){
                    case SPEAKER_STAGE: //FIXME: Determine if simple sequence is possible or if seperate one is necessary
                    case SPEAKER_CENTER:
                        switch(mStage) {
                            case 0:
                                Console.logMsg("Starting Sequence \"" + toString() + "\" [" + mSelectedStartingPosition.toString() + "]");
                                mStage++; break;
                            case 1: 
                                if(tmrTimeout.get() > mSelectedStartDelay.time) mStage++;
                                break;
                            case 2:
                                Console.logMsg("Enabling Reels...");
                                Runway.enableReels_Speaker();
                                tmrTimeout.reset();
                                mStage++; break;
                            case 3:
                                if(tmrTimeout.get() > 0.25) mStage++;
                                break;
                            case 4:
                                Console.logMsg("Time reached. Firing...");
                                Runway.enableDirector();
                                tmrTimeout.reset();
                                mStage++; break;
                            case 5:
                                if(tmrTimeout.get() > 0.25) mStage++;
                                break;
                            case 6:
                                Console.logMsg("Time reached, stopping Runway and driving back 8/4ft...");
                                Runway.disable();
                                Chassis.resetDistance();
                                Chassis.goToDistance(mSelectedStartingPosition == StartingPosition.SPEAKER_STAGE ? 96.0 : 48.0); //FIXME: Determine proper distance
                                tmrTimeout.reset();
                                mStage++; break;
                            case 7:
                                if(Chassis.isAtDistance() || tmrTimeout.get() > (mSelectedStartingPosition == StartingPosition.SPEAKER_STAGE ? 4.0 : 2.0)) mStage++;
                                break;
                            case 8:
                                Console.logMsg("Distance reached, stopping drive...");
                                Chassis.disable();
                                mStage++; break;
                            case 9:
                                Console.logMsg("Sequence Complete");
                                mStage++; break;
                            default:
                                Robot.disableSubsystems();
                        } break;
                    case SPEAKER_WALL:
                        switch(mStage) {
                            case 0:
                                Console.logMsg("Starting Sequence \"" + toString() + "\" [" + StartingPosition.SPEAKER_WALL.toString() + "]");
                                mStage++; break;
                            case 1: 
                                if(tmrTimeout.get() > mSelectedStartDelay.time) mStage++;
                                break;
                            case 2:
                                Console.logMsg("Enabling Reels...");
                                Runway.enableReels_Speaker();
                                tmrTimeout.reset();
                                mStage++; break;
                            case 3:
                                if(tmrTimeout.get() > 0.25) mStage++;
                                break;
                            case 4:
                                Console.logMsg("Time reached. Firing...");
                                Runway.enableDirector();
                                tmrTimeout.reset();
                                mStage++; break;
                            case 5:
                                if(tmrTimeout.get() > 0.25) mStage++;
                                break;
                            case 6:
                                Console.logMsg("Time reached, stopping Runway and driving back 1ft...");
                                Runway.disable();
                                Chassis.resetDistance();
                                Chassis.goToDistance(12.0); //FIXME: Determine if far enough to turn
                                tmrTimeout.reset();
                                mStage++; break;
                            case 7:
                                if(Chassis.isAtDistance() || tmrTimeout.get() > 0.5) mStage++;
                                break;
                            case 8:
                                Console.logMsg("Distance reached, stopping drive and turning 150°...");
                                Chassis.disablePID_Distance();
                                Chassis.resetAngle();
                                Chassis.goToAngle(DriverStation.getAlliance().get() == Alliance.Red ? 150.0 : -150.0); //FIXME: Determine proper angle
                                tmrTimeout.reset();
                                mStage++; break;
                            case 9:
                                if(Chassis.isAtAngle() || tmrTimeout.get() > 1.0) mStage++;
                                break;
                            case 10:
                                Console.logMsg("Angle reached, stopping turn and driving forward 4ft...");
                                Chassis.disablePID_Angle();
                                Chassis.resetDistance();
                                Chassis.goToDistance(48.0);
                                tmrTimeout.reset();
                                mStage++; break;
                            case 11:
                                if(Chassis.isAtDistance() || tmrTimeout.get() > 2.0) mStage++;
                                break;
                            case 12:
                                Console.logMsg("Distance reached, stopping drive...");
                                Chassis.disable();
                                mStage++; break;
                            case 13:
                                Console.logMsg("Sequence Complete");
                                mStage++; break;
                            default:
                                Robot.disableSubsystems();
                        } break;
                    default:
                        switch(mStage) {
                            case 0:
                                Console.logMsg("Invalid Starting Position selected for Sequence \"" + toString());
                                mStage++;
                            default:
                                Robot.disableSubsystems();
                        }
                }
            }},
        /** Shoot into the Speaker, turn around, and drive as far across the field as far as possible, ready to go to the Source. */
        SPEAKER_SHOT_FIELD("Speaker Shot & Cross Field"){
            @Override public void periodic() {
                switch(mSelectedStartingPosition) {
                    case SPEAKER_STAGE:
                        switch(mStage) {
                            //TODO: SPEAKER_SHOT_FIELD - Stage Side
                        } break;
                    case SPEAKER_CENTER:
                        switch(mStage) {
                            case 0:
                                Console.logMsg("Starting Sequence \"" + toString() + "\" [" + StartingPosition.SPEAKER_CENTER.toString() + "]");
                                mStage++; break;
                            case 1: 
                                if(tmrTimeout.get() > mSelectedStartDelay.time) mStage++;
                                break;
                            case 2:
                                Console.logMsg("Enabling Reels...");
                                Runway.enableReels_Speaker();
                                tmrTimeout.reset();
                                mStage++; break;
                            case 3:
                                if(tmrTimeout.get() > 0.25) mStage++;
                                break;
                            case 4:
                                Console.logMsg("Time reached. Firing...");
                                Runway.enableDirector();
                                tmrTimeout.reset();
                                mStage++; break;
                            case 5:
                                if(tmrTimeout.get() > 0.25) mStage++;
                                break;
                            case 6:
                                Console.logMsg("Time reached, stopping Runway and driving back 10ft...");
                                Runway.disable();
                                Chassis.resetDistance();
                                Chassis.goToDistance(120.0); //FIXME: Determine if far enough to turn
                                tmrTimeout.reset();
                                mStage++; break;
                            case 7:
                                if(Chassis.isAtDistance() || tmrTimeout.get() > 5.0) mStage++;
                                break;
                            case 8:
                                Console.logMsg("Distance reached, stopping drive and turning 150°...");
                                Chassis.disablePID_Distance();
                                Chassis.resetAngle();
                                Chassis.goToAngle(DriverStation.getAlliance().get() == Alliance.Red ? 150.0 : -150.0); //FIXME: Determine proper angle
                                tmrTimeout.reset();
                                mStage++; break;
                            case 9:
                                if(Chassis.isAtAngle() || tmrTimeout.get() > 1.0) mStage++;
                                break;
                            case 10:
                                Console.logMsg("Angle reached, stopping turn and driving forward 8ft...");
                                Chassis.disablePID_Angle();
                                Chassis.resetDistance();
                                Chassis.goToDistance(96.0);
                                tmrTimeout.reset();
                                mStage++; break;
                            case 11:
                                if(Chassis.isAtDistance() || tmrTimeout.get() > 4.0) mStage++;
                                break;
                            case 12:
                                Console.logMsg("Distance reached, stopping drive...");
                                Chassis.disable();
                                mStage++; break;
                            case 13:
                                Console.logMsg("Sequence Complete");
                                mStage++; break;
                            default:
                                Robot.disableSubsystems();
                        } break;
                    case SPEAKER_WALL:
                        switch(mStage) {
                            case 0:
                                Console.logMsg("Starting Sequence \"" + toString() + "\" [" + StartingPosition.SPEAKER_CENTER.toString() + "]");
                                mStage++; break;
                            case 1: 
                                if(tmrTimeout.get() > mSelectedStartDelay.time) mStage++;
                                break;
                            case 2:
                                Console.logMsg("Enabling Reels...");
                                Runway.enableReels_Speaker();
                                tmrTimeout.reset();
                                mStage++; break;
                            case 3:
                                if(tmrTimeout.get() > 0.25) mStage++;
                                break;
                            case 4:
                                Console.logMsg("Time reached. Firing...");
                                Runway.enableDirector();
                                tmrTimeout.reset();
                                mStage++; break;
                            case 5:
                                if(tmrTimeout.get() > 0.25) mStage++;
                                break;
                            case 6:
                                Console.logMsg("Time reached, stopping Runway and driving back 1ft...");
                                Runway.disable();
                                Chassis.resetDistance();
                                Chassis.goToDistance(12.0); //FIXME: Determine if far enough to turn
                                tmrTimeout.reset();
                                mStage++; break;
                            case 7:
                                if(Chassis.isAtDistance() || tmrTimeout.get() > 0.5) mStage++;
                                break;
                            case 8:
                                Console.logMsg("Distance reached, turning 30°...");
                                Chassis.disablePID_Distance();
                                Chassis.resetAngle();
                                Chassis.goToAngle(DriverStation.getAlliance().get() == Alliance.Red ? -30.0 : 30.0); //FIXME: Determine proper angle
                                tmrTimeout.reset();
                                mStage++; break;
                            case 9:
                                if(Chassis.isAtAngle() || tmrTimeout.get() > 1.0) mStage++;
                                break;
                            case 10:
                                Console.logMsg("Angle reached, driving back 5ft...");
                                Chassis.resetDistance();
                                Chassis.goToDistance(60.0); //FIXME: Determine proper distance
                                tmrTimeout.reset();
                                mStage++; break;
                            case 11:
                                if(Chassis.isAtDistance() || tmrTimeout.get() > 2.5) mStage++;
                                break;
                            case 12:
                                Console.logMsg("Distance reached, stopping drive and turning 130°...");
                                Chassis.disablePID_Distance();
                                Chassis.resetAngle();
                                Chassis.goToAngle(DriverStation.getAlliance().get() == Alliance.Red ? 130.0 : -130.0); //FIXME: Determine proper angle
                                tmrTimeout.reset();
                                mStage++; break;
                            case 13:
                                if(Chassis.isAtAngle() || tmrTimeout.get() > 1.0) mStage++;
                                break;
                            case 14:
                                Console.logMsg("Angle reached, stopping turn and driving forward 10ft...");
                                Chassis.disablePID_Angle();
                                Chassis.resetDistance();
                                Chassis.goToDistance(120.0);
                                tmrTimeout.reset();
                                mStage++; break;
                            case 15:
                                if(Chassis.isAtDistance() || tmrTimeout.get() > 5.0) mStage++;
                                break;
                            case 16:
                                Console.logMsg("Distance reached, stopping drive...");
                                Chassis.disable();
                                mStage++; break;
                            case 17:
                                Console.logMsg("Sequence Complete");
                                mStage++; break;
                            default:
                                Robot.disableSubsystems();
                        } break;
                    default:
                        switch(mStage) {
                            case 0:
                                Console.logMsg("Invalid Starting Position selected for Sequence \"" + toString());
                                mStage++;
                            default:
                                Robot.disableSubsystems();
                        }
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
            Chassis.enableBrake();
        }

        public abstract void periodic();

        @Override public String toString() { return label; }
    }

    //Parent NetworkTable
    private static final NetworkTable tblAutonomous = Robot.tblPeriod.getSubTable("Autonomous");

    //Dashboard Objects
    private static Chooser<StartingPosition> chsStartingPosisiton = new Chooser<StartingPosition>(tblAutonomous, "Starting Position", StartingPosition.STATION_WALL);
    private static Chooser<StartDelay> chsStartDelay = new Chooser<StartDelay>(tblAutonomous, "Start Delay", StartDelay.ZERO);
    private static Chooser<Sequence> chsSequence = new Chooser<Sequence>(tblAutonomous, "Sequence", Sequence.DO_NOTHING);

    //Dashboard selections
    private static StartingPosition mSelectedStartingPosition;
    private static StartDelay mSelectedStartDelay;
    private static Sequence mSelectedSequence;

    /** Private constructor to prevent individual instances from being created */
    private Autonomous() {}

    /** Runs once when Teleoperated is enabled to read period options from the Dashboard */
    public static void init() {
        //Clear any flags on controls
        ButtonManager.clearFlags();

        mSelectedStartingPosition = chsStartingPosisiton.get();
        mSelectedStartDelay = chsStartDelay.get();
        mSelectedSequence = chsSequence.get();

        mSelectedSequence.init();
    }

    /** Initialize Dashboard values */
    public static void initDashboard() {
        chsStartingPosisiton.init();
        chsStartDelay.init();
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
