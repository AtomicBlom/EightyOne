using System;
using RoomEditor.Framework;

namespace RoomEditor.Model
{
    public class RoomSetPatch : BindableBase
    {
        public PatchedRoom[] Rooms { get; set; }
        public int AffectedRoomId { get; set; }
        public int ConditionRoomId { get; set; }
        public Direction Direction { get; set; }
    }

    public enum Direction
    {
        North,
        South,
        East,
        West
    }

    public static class DirectionExtensions
    {
        public static Direction Opposite(this Direction direction)
        {
            switch (direction)
            {
                case Direction.East: return Direction.West;
                case Direction.West: return Direction.East;
                case Direction.North: return Direction.South;
                case Direction.South: return Direction.North;
            }
            throw new ArgumentException();
        }

        private static readonly Func<int, int> Closest = (x) => 0;
        private static readonly Func<int, int> Current = (x) => x;

        public static (Func<int, int> xOffset, Func<int, int> zOffset) GetPrimaryRoomOffset(this Direction direction, int roomSize)
        {
            int Furthest(int x) => roomSize - 1;
            
            switch (direction)
            {
                case Direction.North:
                    return (Current, Closest);
                case Direction.South:
                    return (Current, Furthest);
                case Direction.East:
                    return (Furthest, Current);
                case Direction.West:
                    return (Closest, Current);
            }
            
            throw new ArgumentException();
        }

        public static (Func<int, int> xOffset, Func<int, int> zOffset) GetTestRoomOffset(this Direction direction, int roomSize)
        {
            int Furthest(int x) => roomSize - 1;

            switch (direction)
            {
                case Direction.North:
                    return (Current, Furthest);
                case Direction.South:
                    return (Current, Closest);
                case Direction.East:
                    return (Closest, Current);
                case Direction.West:
                    return (Furthest, Current);
            }

            throw new ArgumentException();
        }
    }


}