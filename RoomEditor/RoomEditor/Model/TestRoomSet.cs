using System.Collections.Generic;

namespace RoomEditor.Model
{
    public class TestRoomSet
    {
        public RoomSet RoomSet { get; set; }
        public Dictionary<Direction, RoomSetPatch> Patches { get; set; }
    }
}