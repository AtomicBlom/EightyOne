using System.Windows.Input;

namespace RoomEditor.Framework
{
    /// <summary>
    /// Extension of <see cref="ICommand"/> to allow manually call of <see cref="ICommand.CanExecuteChanged"/>
    /// </summary>
    public interface IChangedCommand:ICommand
    {
        void RaiseCanExecuteChanged();
    }
}