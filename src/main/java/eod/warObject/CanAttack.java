package eod.warObject;

import eod.exceptions.NotSupportedException;
import eod.param.AttackParam;

import java.awt.*;
import java.util.ArrayList;

public interface CanAttack {
    String ERR_MSG = "This method isn't supported by the object.";

    default void attack() throws NotSupportedException {
        throw new NotSupportedException(ERR_MSG);
    }

    default ArrayList<Damageable> attack(ArrayList<Point> targets, AttackParam param) throws NotSupportedException {
        throw new NotSupportedException(ERR_MSG);
    }

    default ArrayList<Damageable> attack(Damageable target, AttackParam param) throws NotSupportedException {
        throw new NotSupportedException(ERR_MSG);
    }
    default ArrayList<Damageable> attack(Damageable[] targets, AttackParam param) throws NotSupportedException {
        throw new NotSupportedException(ERR_MSG);
    }

    default ArrayList<Point> getAttackRange() throws NotSupportedException {
        throw new NotSupportedException(ERR_MSG);
    }

    void addAttack(int a);
}
