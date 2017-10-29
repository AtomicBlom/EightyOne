namespace RoomEditor.Services
{
    public class IORoomSetFile
    {
        public int Width { get; set; }
        public int Height { get; set; }
        public IORoomSet[] RoomSets { get; set; }
    }
}