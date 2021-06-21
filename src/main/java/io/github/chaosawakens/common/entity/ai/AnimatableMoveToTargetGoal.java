package io.github.chaosawakens.common.entity.ai;

import java.util.EnumSet;

import io.github.chaosawakens.common.entity.AnimatableMonsterEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EntityPredicates;

/**
 * Move a given entity towards another targeted entity
 * @author invalid2
 */
public class AnimatableMoveToTargetGoal extends AnimatableGoal {
	
	private double moveSpeed;
	private int checkRate;
	private Path path;
	
	/**
	 * 
	 */
	public AnimatableMoveToTargetGoal(AnimatableMonsterEntity entity, double moveSpeed, int checkRate) {
		this.entity = entity;
		this.moveSpeed = moveSpeed;
		this.checkRate = checkRate;
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}
	
	@Override
	public boolean shouldExecute() {
		this.baseTick();
		if(this.animationProgress/50000 < this.checkRate)return false;
		
		this.animationProgress = 0;
		return AnimatableMoveToTargetGoal.checkIfValid(this, this.entity, this.entity.getAttackTarget());
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		this.baseTick();
		if(this.entity.getAttackTarget() != null)if(this.animationProgress/25000 < this.checkRate)return true;
		
		boolean check = AnimatableMoveToTargetGoal.checkIfValid(this, this.entity, this.entity.getAttackTarget());
		return check;
	}
	
	@Override
	public void startExecuting() {
		this.entity.setAggroed(true);
		this.entity.getNavigator().setPath(this.path, this.moveSpeed);
		this.animationProgress = 0;
	}
	
	@Override
	public void resetTask() {
		LivingEntity target = this.entity.getAttackTarget();
		if (!EntityPredicates.CAN_AI_TARGET.test(target)) {
			this.entity.setAttackTarget(null);
		}
		this.animationProgress = 0;
		this.entity.setAggroed(false);
		this.entity.getNavigator().clearPath();
	}
	
	@Override
	public void tick() {
		this.baseTick();
		LivingEntity target = this.entity.getAttackTarget();
		this.entity.getLookController().setLookPositionWithEntity(target, 30F, 30F);
		this.entity.getNavigator().tryMoveToEntityLiving(target, this.moveSpeed);
	}
	
	private static boolean checkIfValid(AnimatableMoveToTargetGoal goal, AnimatableMonsterEntity attacker, LivingEntity target) {
		if(target == null)return false;
		if(target.isAlive() && !target.isSpectator()) {
			if(target instanceof PlayerEntity && ((PlayerEntity) target).isCreative())return false;
			
			double distance = goal.entity.getDistanceSq(target.getPosX(), target.getPosY(), target.getPosZ());
			goal.path = attacker.getNavigator().pathfind(target, 0);
			
			if(attacker.getEntitySenses().canSee(target) && distance >= AnimatableGoal.getAttackReachSq(attacker, target)) {
				return true;
			}
		}
		return false;
	}
}