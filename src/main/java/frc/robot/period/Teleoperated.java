package frc.robot.period;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.molib.buttons.Button;
import frc.molib.buttons.ButtonManager;
import frc.molib.dashboard.Chooser;
import frc.molib.hid.XboxController;
import frc.robot.Robot;
import frc.robot.subsystem.Chassis;
import frc.robot.subsystem.Hanger;
import frc.robot.subsystem.Runway;

/** Teleoperated Period: Handles human input to control the robot */
@SuppressWarnings({"unused", "deprecation"})
public class Teleoperated {

    /** Preset scales to reduce the power to the Chassis for less experienced drivers */
    private static enum ChassisPowerScale {
        TORTOISE("Tortoise - 20%",  0.10, 0.20, 0.40),
        EASY("Easy - 50%",          0.25, 0.50, 0.75),
        NORMAL("Normal - 75%",      0.50, 0.75, 0.90),
        EXPERT("Expert - 90%",      0.75, 0.90, 1.00);

        public final String label;
        public final double slow, standard, boost;

        private ChassisPowerScale(String label, double slow, double standard, double boost) { 
            this.label = label;
            this.slow = slow;
            this.standard = standard;
            this.boost = boost;
        }

        @Override
        public String toString() { return label; }
    }

    /** Offers various ways of controlling the Chassis */
	private static enum ChassisControlMode {
		ARCADE("Arcade Drive"),
		CHEEZY("Cheezy Drive"),
		TANK("Tank Drive");

		public final String label;

		private ChassisControlMode(String label){
			this.label = label;
		}

        @Override
        public String toString() { return label; }
	}

    //Parent NetworkTable
    private static final NetworkTable tblTeleoperated = Robot.tblPeriod.getSubTable("Teleoperated");

    private static Chooser<ChassisPowerScale> chsChassisPowerScale = new Chooser<ChassisPowerScale>(tblTeleoperated, "Chassis Power Scale", ChassisPowerScale.NORMAL);
    private static Chooser<ChassisControlMode> chsChassisControlMode = new Chooser<ChassisControlMode>(tblTeleoperated, "Chassis Control Mode", ChassisControlMode.TANK);

	private static ChassisPowerScale mSelectedChassisPowerScale;
	private static ChassisControlMode mSelectedChassisControlMode;

    private static final XboxController ctlDriver = new XboxController(0);
    private static final XboxController ctlOperator = new XboxController(1);

    //Driver Buttons
    private static final Button btnDrive_Slow = new Button() { @Override public boolean get() { return ctlDriver.getLeftBumper(); } };
    private static final Button btnDrive_Boost = new Button() { @Override public boolean get() { return ctlDriver.getRightBumper(); } };
    private static final Button btnDrive_Brake = new Button() { @Override public boolean get() { return ctlDriver.getLeftTrigger(); } };
    private static final Button btnHanger_Extend = new Button() { @Override public boolean get() { return ctlDriver.getBButton(); } };
    private static final Button btnHanger_Retract = new Button() { @Override public boolean get() { return ctlDriver.getAButton(); } };
    private static final Button btnShoot = new Button() { @Override public boolean get() { return ctlDriver.getRightTrigger(); } };

    //Operator Buttons
    private static final Button btnReels_SpeakerShot = new Button() { @Override public boolean get() { return ctlOperator.getRightTrigger(); } };
    private static final Button btnReels_AmpShot = new Button() { @Override public boolean get() { return ctlOperator.getRightBumper(); } };
    private static final Button btnIntake = new Button() { @Override public boolean get() { return ctlOperator.getLeftTrigger(); } };
    private static final Button btnOuttake = new Button() { @Override public boolean get() { return ctlOperator.getXButton(); } };
    private static final Button btnGroundPickUp_Lower = new Button() { @Override public boolean get() { return ctlOperator.getAButton(); } };

    /** Private constructor to prevent individual instances from being created */
    private Teleoperated() {}

    /** Runs once when Teleoperated is enabled to read period options from the Dashboard */
    public static void init() {
        //Clear any flags on controls
        ButtonManager.clearFlags();

        //Get selected options from Dashboard
		mSelectedChassisPowerScale = chsChassisPowerScale.get();
		mSelectedChassisControlMode = chsChassisControlMode.get();
    }

    /** Initialize Dashboard values */
    public static void initDashboard() {
        chsChassisPowerScale.init();
        chsChassisControlMode.init();
    }

    /** Call regularly to push new values to Dashboard */
    public static void updateDashboard() {

    }

	/**
     * Apply Tank Drive style of control to the Chassis
     * @param powerLeft [-1.0 to 1.0] Power to left side of the Chassis
     * @param powerRight [-1.0 to 1.0] Power to right side of the Chassis
     */
    public static void setTankDrive(double powerLeft, double powerRight){
        //Chassis.setDrive(Math.signum(powerLeft) * (powerLeft * powerLeft), Math.signum(powerRight) * (powerRight * powerRight));
        Chassis.setDrivePower(powerLeft, powerRight);
    }

    /**
     * Apply Arcade Drive style of control to the Chassis
     * @param throttle [-1.0 to 1.0] Forward/Reverse power
     * @param steering [-1.0 to 1.0] Left/Right turning strength
     */
    public static void setArcadeDrive(double throttle, double steering){
        setTankDrive(throttle + steering, throttle - steering);
    }

//////////////////////////////////////////////////
    
    /** Call regularly to update PIDs, interpret sensor input, and apply buffered values to components. */
    public static void periodic() {
        //Chassis Power Scaling
        double chassisScale;
        if(btnDrive_Slow.get()) 
            chassisScale = mSelectedChassisPowerScale.slow;
        else if(btnDrive_Boost.get()) 
            chassisScale = mSelectedChassisPowerScale.boost;
        else
            chassisScale = mSelectedChassisPowerScale.standard;

		//Chassis Drive Control
		switch(mSelectedChassisControlMode){
			case TANK: setTankDrive(ctlDriver.getLeftY() * chassisScale, ctlDriver.getRightY() * chassisScale); break;
			case CHEEZY: setArcadeDrive(ctlDriver.getLeftY() * chassisScale, ctlDriver.getLeftX() * chassisScale); break;
			case ARCADE: setArcadeDrive(ctlDriver.getLeftY() * chassisScale, ctlDriver.getRightX() * chassisScale); break;
        }

        //Chassis Brake
        if(btnDrive_Brake.getPressed())
            Chassis.enableBrake();
        else if(btnDrive_Brake.getReleased())
            Chassis.disableBrake();

		//Runway Control
		if(btnReels_AmpShot.get()) {
            Runway.disableLEDs();
            Runway.enableReels_Amp();
            if(btnShoot.get()) 
                Runway.enableDirector();
            else 
                Runway.disableDirector();
        } else if(btnReels_SpeakerShot.get()) {
            Runway.disableLEDs();
            Runway.enableReels_Speaker();
            if(btnShoot.get()) 
                Runway.enableDirector();
            else 
                Runway.disableDirector();
        } else if(btnIntake.get()) {
            Runway.enableLEDs();
            Runway.reverseReels();
            Runway.reverseDirector();
        } else {
            Runway.disable();
        }

        //Hanger Control
        if(btnHanger_Extend.get()) 
            Hanger.extendWinch();
        else if(btnHanger_Retract.get()) 
            Hanger.retractWinch();
        else 
            Hanger.disableWinch();

        Chassis.periodic();
		Runway.periodic();
        Hanger.periodic();
    }
}
