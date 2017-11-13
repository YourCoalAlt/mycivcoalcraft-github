/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.object;

/* Any object with a balance that can go into debt. */
public class EconObject {

	private String econName;
	private Integer coins = 0;
	private Integer debt = 0;
	private Integer principalAmount = 0;
	private SQLObject holder;
	
	public EconObject(SQLObject holder) {
		this.holder = holder;
	}
	
	public String getEconomyName() {
		return econName;
	}
	
	public void setEconomyName(String name) {
		this.econName = name;
	}
	
	public Integer getBalance() {
		synchronized (coins) {
			return coins;
		}
	}
	
	public void setBalance(int amount) {
		this.setBalance(amount, true);
	}
	
	public void setBalance(int amount, boolean save) {
		if (amount < 0) {
			amount = 0;
		}
		
		synchronized (coins) {
			coins = amount;
		}
		
		if (save) {
			holder.save();
		}
	}
	
	public void deposit(int amount) {
		if (amount < 0) {
			amount = 0;
		}
		this.deposit(amount, true);
	}
	
	public void deposit(int amount, boolean save) {
		if (amount < 0) {
			amount = 0;
		}
		
		synchronized (coins) {
			coins += amount;
		}
		
		if (save) {
			holder.save();
		}
	}
	
	public void withdraw(int amount) {
		if (amount < 0) {
			amount = 0;
		}
		this.withdraw(amount, true);
	}
	
	public void withdraw(int amount, boolean save) {
		if (amount < 0) {
			amount = 0;
		}
		
		/* Update the principal we use to calculate interest,
		 * if our current balance dips below the principal,
		 * then we subtract from the principal. */
		synchronized(principalAmount) {
			if (principalAmount > 0) {
				int currentBalance = this.getBalance();
				int diff = currentBalance - principalAmount;
				diff -= amount;
				
				if (diff < 0) {
					principalAmount -= (-diff);
				}
			}
		}
		
		synchronized(coins) {
			coins -= amount;
		}
		
		if (save) {
			holder.save();
		}
	}
	
	public boolean hasEnough(int amount) {
		synchronized (coins) {
			if (coins >= amount) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public boolean payTo(EconObject objToPay, int amount) {
		if (!this.hasEnough(amount)) {
			return false;
		} else {
			this.withdraw(amount);
			objToPay.deposit(amount);
			return true;
		}
	}
	
	public int payToCreditor(EconObject objToPay, int amount) {
		int total = 0;
		
		if (this.hasEnough(amount)) {
			this.withdraw(amount);
			objToPay.deposit(amount);
			return amount;
		}
		
		// Do not have enough to pay, pay what we can and put the rest into debt.
		this.debt += amount - this.getBalance();
		objToPay.deposit(this.getBalance());
		this.withdraw(this.getBalance());
		return total;
	}
	
	public boolean inDebt() {
		if (debt > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getDebt() {
		return debt;
	}
	
	public void setDebt(int debt) {
		this.debt = debt;
	}
	
	public int getPrincipalAmount() {
		return principalAmount;
	}
	
	public void setPrincipalAmount(int interestAmount) {
		this.principalAmount = interestAmount;
	}
}
