package frc.robot.subsystem;

import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import frc.molib.PIDController;
import frc.molib.dashboard.Entry;
import frc.molib.utilities.Console;
import frc.robot.Robot;

/** Chassis Subsystem: Handles making the robot drive. */
public class Chassis {
	//Parent NetworkTable
	private static final NetworkTable tblChassis = Robot.tblSubsystem.getSubTable("Chassis");

	//Dashboard Values
	private static final Entry<Double> entDrive_Distance = new Entry<Double>(tblChassis, "Drive Distance");
	private static final Entry<Double> entDrive_Angle = new Entry<>(tblChassis, "Drive Angle");

	//Motors
	private static final TalonFX mtrDrive_L1 = new TalonFX(1);
	private static final TalonFX mtrDrive_L2 = new TalonFX(2);
	private static final TalonFX mtrDrive_R1 = new TalonFX(3);
	private static final TalonFX mtrDrive_R2 = new TalonFX(4);

	//Sensors
	private static final ADXRS450_Gyro gyrDrive_Angle = new ADXRS450_Gyro();

	//PID Controllers
	private static final PIDController pidDrive_Distance = new PIDController(0, 0, 0);
	private static final PIDController pidDrive_Angle = new PIDController(0, 0, 0);

	//Constants
	private static final double DRIVE_GEAR_RATIO = 1.0/6.28;

	//Power Buffer Variables
	private static double mDrivePower_L = 0.0;
	private static double mDrivePower_R = 0.0;

////System-wide Control

	/** Private constructor to prevent individual instances from being created */
	private Chassis() {}

	/** Runs once at Robot startup to configure subsystem components */
	public static void init() {
		Console.logMsg("Chassis Subsystem Initializing...");

		//Motor inversions
		mtrDrive_L1.setInverted(true);
		mtrDrive_L2.setInverted(true);
		mtrDrive_R1.setInverted(false);
		mtrDrive_R2.setInverted(false);

		//Motor NeutralMode configuration
		mtrDrive_L1.setNeutralMode(NeutralModeValue.Coast);
		mtrDrive_L2.setNeutralMode(NeutralModeValue.Coast);
		mtrDrive_R1.setNeutralMode(NeutralModeValue.Coast);
		mtrDrive_R2.setNeutralMode(NeutralModeValue.Coast);

		//Motor Follower configuration
		mtrDrive_L2.setControl(new Follower(mtrDrive_L1.getDeviceID(), false));
		mtrDrive_R2.setControl(new Follower(mtrDrive_R1.getDeviceID(), false));

		//Gyro calibration
		Console.logMsg("Calibrating Gyro, please don't move the robot...");
		gyrDrive_Angle.calibrate();
		Console.logMsg("Gyro Calibrated");

		Console.logMsg("Chassis Initialization Complete");
	}

	/** Initialize Dashboard values */
	public static void initDashboard() {
		entDrive_Distance.set(getDistance());
		entDrive_Angle.set(getAngle());
	}

	/** Call regularly to push new values to Dashboard */
	public static void updateDashboard() {
		entDrive_Distance.set(getDistance());
		entDrive_Angle.set(getAngle());
	}

	/** Disable the entire subsystem */
	public static void disable() {
		disableDrive();
		disablePIDs();
	}

	/** Release Chassis control from all PIDs */
	public static void disablePIDs() {
		disablePID_Distance();
		disablePID_Angle();
	}

////Sensors

	/** 
	 * Get the averaged drive distance since last reset 
	 * @return Distance driven in inches
	 */
	public static double getDistance() {
		//Average position of all drive motors, multiplied by the gearbox ration, multiplied by the circumference of the wheels
		return ((mtrDrive_L1.getPosition().getValue() + mtrDrive_L2.getPosition().getValue() + mtrDrive_R1.getPosition().getValue() + mtrDrive_R2.getPosition().getValue()) / 4.0 ) * DRIVE_GEAR_RATIO * (Math.PI * 4.0);
	}

	/** Reset all Drive Motor positions to 0.0 */
	public static void resetDistance() {
		mtrDrive_L1.setPosition(0.0);
		mtrDrive_L2.setPosition(0.0);
		mtrDrive_R1.setPosition(0.0);
		mtrDrive_R2.setPosition(0.0);
	}

	/**
	 * Get whether the distance PID has reached its target
	 * @return True if on target for target time
	 */
	public static boolean isAtDistance() {
		return pidDrive_Distance.atSetpoint();
	}

	/** 
	 * Get the chassis angle since last reset 
	 * @return Angle turned in degrees
	 */
	public static double getAngle() {
		return gyrDrive_Angle.getAngle();
	}

	/** Reset Gyro angle to 0.0 */
	public static void resetAngle() {
		gyrDrive_Angle.reset();
	}

	/**
	 * Get whether the angle PID has reached its target
	 * @return True if on target for target time
	 */
	public static boolean isAtAngle() {
		return pidDrive_Angle.atSetpoint();
	}

////Drive

	/**
	 * Apply power to the left and right Drive motors
	 * @param powerLeft		[-1.0 to 1.0] Power to the left Drive motors
	 * @param powerRight	[-1.0 to 1.0] Power to the right Drive motors
	 */
	public static void setDrivePower(double powerLeft, double powerRight) {
		mDrivePower_L = powerLeft;
		mDrivePower_R = powerRight;
	}

	/** Turn off Drive motors */
	public static void disableDrive() {
		setDrivePower(0.0, 0.0);
	}

	/** Set all Drive motors to Brake mode */
	public static void enableBrake() {
		mtrDrive_L1.setNeutralMode(NeutralModeValue.Brake);
		mtrDrive_L2.setNeutralMode(NeutralModeValue.Brake);
		mtrDrive_R1.setNeutralMode(NeutralModeValue.Brake);
		mtrDrive_R2.setNeutralMode(NeutralModeValue.Brake);
	}

	/** Set all Drive motors to Coast mode */
	public static void disableBrake() {
		mtrDrive_L1.setNeutralMode(NeutralModeValue.Coast);
		mtrDrive_L2.setNeutralMode(NeutralModeValue.Coast);
		mtrDrive_R1.setNeutralMode(NeutralModeValue.Coast);
		mtrDrive_R2.setNeutralMode(NeutralModeValue.Coast);
	}

////Distance PID

	/**
	 * Drive to a specific distance, straight forward
	 * @param distance Target distance in inches
	 */
	public static void goToDistance(double distance) {
		pidDrive_Distance.enable();
		pidDrive_Distance.setSetpoint(distance);
	}

	/** Release Chassis control from the Distance PID */
	public static void disablePID_Distance() {
		pidDrive_Distance.disable();
	}

////Angle PID

	/**
	 * Turn to a specific angle
	 * @param angle Target angle in degrees
	 */
	public static void goToAngle(double angle) {
		pidDrive_Angle.enable();
		pidDrive_Angle.setSetpoint(angle);
	}

	/** Release Chassis control from the Angle PID */
	public static void disablePID_Angle() {
		pidDrive_Angle.disable();
	}

//////////////////////////////////////////////////

	/** Call regularly to update PIDs, interpret sensor input, and apply buffered values to components. */
	public static void periodic() {
		//Evaluate PID control
		if(pidDrive_Distance.isEnabled()) //Override with Distance PID value
			setDrivePower(pidDrive_Distance.calculate(getDistance()), pidDrive_Distance.calculate(getDistance()));
		else if(pidDrive_Angle.isEnabled()) //Override with Angle PID value
			setDrivePower(pidDrive_Angle.calculate(getAngle()), pidDrive_Angle.calculate(getAngle()));

		//Apply buffer values
		mtrDrive_L1.set(mDrivePower_L);
		mtrDrive_R1.set(mDrivePower_R);
	}
}
