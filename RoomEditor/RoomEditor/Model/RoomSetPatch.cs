using RoomEditor.Framework;

namespace RoomEditor.Model
{
    public class RoomSetPatch : BindableBase
    {
        public PatchedRoom[] Rooms { get; set; }
        public int AffectedRoomId { get; set; }
        public int ConditionRoomId { get; set; }
    }
}