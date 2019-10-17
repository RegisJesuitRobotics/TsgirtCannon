
package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TShirtControl implements Runnable {

	boolean tankCharged;
	WPI_TalonSRX L1, L2, L3, R1, R2, R3, TurningMotor, ElevationMotorLeft, ElevationMotorRight;
	PlaystationController playStation;
	DriverStation driverStation;
	Solenoid[] Barrel;
	DoubleSolenoid doubleSolenoid;
	int ActiveBarrel;
	int waitTime = 4000;
	double timeBetweenR1Presses = 10;
	double timeBetweenR1Presses2 = 10;
	double Limiter;
	Side SideThatsFillingUp;
	boolean startup;
	enum Side {
		LeftSide, RightSide
	};
	enum CannonMovement {
		Up, Down, None
	};

	public TShirtControl(PlaystationController playStation) {
		L1 = new WPI_TalonSRX(4);
		L2 = new WPI_TalonSRX(3);
		R1 = new WPI_TalonSRX(2);
		R2 = new WPI_TalonSRX(1);

		playStation = new PlaystationController(0);

		TurningMotor = new WPI_TalonSRX(7);
		ElevationMotorLeft = new WPI_TalonSRX(5);
		ElevationMotorRight = new WPI_TalonSRX(6);

		Barrel = new Solenoid[6];
		Barrel[0] = new Solenoid(0);
		Barrel[1] = new Solenoid(1);
		Barrel[2] = new Solenoid(2);
		Barrel[3] = new Solenoid(3);
		Barrel[4] = new Solenoid(4);
		Barrel[5] = new Solenoid(5);

		for (Solenoid setSolenoid : Barrel) {
			setSolenoid.setPulseDuration(2.55);
		}

		ActiveBarrel = 1;

		SideThatsFillingUp = Side.LeftSide;
		startup = true;

		doubleSolenoid = new DoubleSolenoid(6, 7);

		
		this.playStation = playStation;
		Limiter = 0.3;
		StartTimer();
	}
    public void CannonMovement() {
		//Horizontal Movement
		if (playStation.ButtonSquare() == true) {
		TurningMotor.set(.25);
		System.out.println("Turning");
		
		} else if (playStation.ButtonCircle() == true) {
		TurningMotor.set(-.25);
		System.out.println("turning 2");
		
		} else {
		TurningMotor.set(0);
        }
    
        CannonMovement cannonMovement = CannonMovement.None;
		
		//vertical movement
        if (playStation.ButtonTriangle() == true) {
            cannonMovement = CannonMovement.Up;

        } else if (playStation.ButtonX() == true) {
            cannonMovement = CannonMovement.Down;
        }

    switch (cannonMovement) {
		case Up:
			ElevationMotorLeft.set(1);
			ElevationMotorRight.set(1);
			break;
		case Down:
			ElevationMotorLeft.set(-1);
			ElevationMotorRight.set(-1);
			break;
		default:
			ElevationMotorLeft.set(0);
			ElevationMotorRight.set(0);
			break;
		}
    
}

	public void TShirtDrive() {
        double RightTrigger = playStation.RightTrigger();
		double LeftTrigger = playStation.LeftTrigger();
		double LeftStick = playStation.LeftStickXAxis();
		boolean isTriangle = playStation.ButtonTriangle();
		double Deadzone = 0.1;
		double RightPower = 1;
		double LeftPower = 1;
		double Power;
		double turn = 2 * LeftStick;
		Power = RightTrigger - LeftTrigger;
		String move = "";
		if (LeftStick > Deadzone) {

			LeftPower = Power;
			RightPower = Power - (turn * Power);
			move = "Left Turn ";
		} else if (LeftStick < -Deadzone) {

			LeftPower = Power + (turn * Power);
			RightPower = Power;
			move = "Right Turn ";
		} else {
			LeftPower = Power;
			RightPower = Power;
			move = "Straight ";
		}
		R1.set(-RightPower * Limiter);
			R2.set(-RightPower * Limiter);

			L1.set(LeftPower * Limiter);
			L2.set(LeftPower * Limiter);
	}

	
		
	public void charging() {

	}

	public void SHOOT() {

		SmartDashboard.putNumber("fireBarrel", ActiveBarrel);

		System.out.println("Shoot Button has been pushed ");
		Barrel[ActiveBarrel].startPulse();
		SwitchBarrel();
		StartTimer();
	
	}

	public void SwitchBarrel() {
		System.out.println("Active Barrel " + ActiveBarrel);
		Barrel[ActiveBarrel].set(true);
		if (ActiveBarrel == 0) {
			ActiveBarrel = 1;
			SideThatsFillingUp = Side.LeftSide;
		} else if (ActiveBarrel == 1) {
			ActiveBarrel = 2;
			SideThatsFillingUp = Side.RightSide;
		} else if (ActiveBarrel == 2) {
			ActiveBarrel = 3;
			SideThatsFillingUp = Side.LeftSide;
		} else if (ActiveBarrel == 3) {
			ActiveBarrel = 4;
			SideThatsFillingUp = Side.RightSide;
		} else if (ActiveBarrel == 4) {
			ActiveBarrel = 5;
			SideThatsFillingUp = Side.LeftSide;
		} else if (ActiveBarrel == 5) {
			ActiveBarrel = 0;
			SideThatsFillingUp = Side.RightSide;
		}

	}

	public void valveCheck(boolean check) {

		Barrel[0].set(check);
		Barrel[1].set(check);
		Barrel[2].set(check);
		Barrel[3].set(check);
		Barrel[4].set(check);
		Barrel[5].set(check);

	}

	@Override
	public void run() {
		System.out.println("Tank charge Runnable Start");
		try {
			setTankCharged(false);
			if (SideThatsFillingUp == Side.LeftSide || startup) {
				doubleSolenoid.set(Value.kForward);
				System.out.println("Charging " + SideThatsFillingUp);
				Thread.sleep(waitTime);

			}
			if (SideThatsFillingUp == Side.RightSide || startup) {
				doubleSolenoid.set(Value.kReverse);
				System.out.println("Charging " + SideThatsFillingUp);
				Thread.sleep(waitTime);

			}
			setTankCharged(true);
			startup = false;

			System.out.println("Tank charge Runnable End");
		} catch (InterruptedException e) {
			e.printStackTrace();

		}

	}

	public boolean isTankCharged() {
		return tankCharged;

	}

	public void setTankCharged(boolean tankCharged) {
		this.tankCharged = tankCharged;
		SmartDashboard.putBoolean("tankCharged", tankCharged);
	}

	public void StartTimer() {
		new Thread(this).start();

	}

}