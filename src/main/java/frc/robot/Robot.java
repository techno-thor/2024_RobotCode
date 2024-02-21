// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.molib.buttons.ButtonManager;
import frc.molib.dashboard.ChooserManager;
import frc.robot.period.Teleoperated;
import frc.robot.subsystem.Chassis;
import frc.robot.subsystem.Hanger;
import frc.robot.subsystem.Runway;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

	//Global Parent NetworkTables
	public static final NetworkTable tblMain = NetworkTableInstance.getDefault().getTable("MO Data");
	public static final NetworkTable tblPeriod = tblMain.getSubTable("Period");
	public static final NetworkTable tblSubsystem = tblMain.getSubTable("Subsystem");

	@Override
	public void robotInit() {
		//Initialize Subsystems
		Chassis.init();
		Runway.init();
		Hanger.init();

		//Wait for NetworkTables connection
		while(!NetworkTableInstance.getDefault().isConnected());

		//Initialize Dashboard values
		Teleoperated.initDashboard();

		Chassis.initDashboard();
		Runway.initDashboard();
		Hanger.initDashboard();
	}

	@Override
	public void robotPeriodic() {
		ChooserManager.updateAll();
		ButtonManager.updateValues();

		//Update Subsystem Dashboard values
		Chassis.updateDashboard();
		Runway.updateDashboard();
		Hanger.updateDashboard();
	}

	@Override
	public void autonomousInit() {}

	@Override
	public void autonomousPeriodic() {}

	@Override
	public void teleopInit() {
		Teleoperated.init();
	}

	@Override
	public void teleopPeriodic() {
		Teleoperated.periodic();
	}

	@Override
	public void disabledInit() {}

	@Override
	public void disabledPeriodic() {}

	@Override
	public void testInit() {}

	@Override
	public void testPeriodic() {}
}
